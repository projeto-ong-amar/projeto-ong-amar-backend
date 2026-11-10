# Funcionamento do SMTP no projeto

O backend (Spring Boot + Jakarta Mail) envia e-mails por **dois canais SMTP independentes**, cada um com sua própria configuração e seu próprio serviço.

| Canal | Classe | Variáveis | Uso |
|---|---|---|---|
| Recuperação de senha | `EmailService` | `MAIL_*` | E-mail com link de redefinição |
| Alertas de estoque | `EmailAlertaService` | `ALERT_MAIL_*` | Estoque baixo, vencimento, entradas/saídas |

Os dois canais não compartilham credenciais nem código: mudar um não afeta o outro.

## 1. Recuperação de senha (`MAIL_*`)

- Configurada via auto-configuração do Spring (`spring.mail.*` em `application.properties`), com `auth` e `starttls` habilitados.
- `EmailService.enviarRecuperacaoSenha(destinatario, nome, link)` monta um `SimpleMailMessage` (texto puro) e envia com o `MailSender` injetado pelo Spring.
- É chamado de forma **síncrona** por `UsuarioService` (linha ~106) ao solicitar a recuperação. O link expira em 15 minutos (informado no corpo do e-mail).
- O link base vem de `PASSWORD_RESET_URL` (padrão `http://localhost:5173/redefinir-senha`).

| Variável | Obrigatória | Descrição |
|---|---|---|
| `MAIL_HOST` | Sim | Host SMTP |
| `MAIL_PORT` | Não (587) | Porta |
| `MAIL_USERNAME` / `MAIL_PASSWORD` | Sim | Credenciais |
| `MAIL_FROM` | Sim | Remetente (`app.email.remetente`) |

## 2. Alertas de estoque (`ALERT_MAIL_*`)

### Configuração

`EmailAlertaService` **não usa** o `JavaMailSender` do Spring: ele cria seu próprio `JavaMailSenderImpl` no construtor, com:

- STARTTLS obrigatório (`mail.smtp.starttls.required=true`) e autenticação;
- timeouts: conexão 5 s, leitura 10 s, escrita 10 s;
- encoding UTF-8;
- e-mails multipart (texto + HTML) via `MimeMessageHelper`.

| Variável | Obrigatória | Padrão | Descrição |
|---|---|---|---|
| `ALERT_MAIL_HOST` | Não | `smtp.gmail.com` | Host SMTP |
| `ALERT_MAIL_PORT` | Não | `587` | Porta (STARTTLS) |
| `ALERT_MAIL_USERNAME` | Para enviar | — | Conta Gmail |
| `ALERT_MAIL_PASSWORD` | Para enviar | — | **Senha de app** do Gmail |
| `ALERT_MAIL_FROM` | Para enviar | — | Remetente |
| `ALERTS_EXPIRY_CRON` | Não | `0 0 8 * * *` | Horário do resumo de vencimento |
| `ALERTS_TIME_ZONE` | Não | `America/Sao_Paulo` | Fuso do agendamento |

Se `USERNAME`, `PASSWORD` ou `FROM` estiverem vazios, o serviço fica "não configurado": `enviar()` lança `IllegalStateException`, que é capturada e apenas registrada em log. A aplicação sobe normalmente.

Veja o passo a passo do Gmail em [README_NOTIFICACOES_GMAIL.md](README_NOTIFICACOES_GMAIL.md).

### Fluxo de envio

```
MovimentacaoService (dentro da transação)
   └─ publica MovimentacaoCriadaEvent / EstoqueAlteradoEvent
         │  (COMMIT no banco)
         ▼
NotificacaoEstoqueService  @TransactionalEventListener(AFTER_COMMIT)
   └─ enfileira tarefa no executor "alertaEmailExecutor"  → retorna imediatamente
         ▼
Thread "alerta-email-N"
   ├─ aplica a regra (limite de estoque, tipo de movimentação...)
   ├─ busca preferências de usuários ativos (PreferenciaNotificacaoRepository)
   ├─ filtra por tipo de alerta, remove e-mails vazios/duplicados
   └─ EmailAlertaService.enviar(...) para cada destinatário  → SMTP
```

Pontos importantes:

1. **Depois do commit**: se a transação sofrer rollback, nenhum e-mail é enviado.
2. **Em segundo plano**: a resposta HTTP não espera o SMTP.
3. **Um e-mail por destinatário**, enviado individualmente (ninguém vê o endereço dos outros).
4. **Falha isolada**: erro em um destinatário é logado e não impede os demais nem desfaz a movimentação.
5. O conteúdo dinâmico no HTML é escapado (`escapar()`) para evitar injeção de HTML.

### Tipos de alerta

| Alerta | Gatilho | Preferência do usuário |
|---|---|---|
| Nova entrada | Movimentação `ENTRADA` | `novasEntradas` |
| Nova saída | Movimentação `SAIDA` | `novasSaidas` |
| Estoque baixo | `EstoqueAlteradoEvent`: estoque atual `<` limite **e** quantidade anterior `≥` limite (só na transição), produto `ATIVO` | `estoqueBaixo` |
| Risco de vencimento | `@Scheduled` diário (`ALERTS_EXPIRY_CRON`), janela de `riscoVencimentoDias`; não envia se não houver lotes | `riscoVencimento` |

`PERDA` não gera alerta de saída, apenas reavalia o estoque baixo.

### Fila (`EmailAlertaExecutorConfig`)

- `ThreadPoolTaskExecutor` com 2 threads, fila de 100 tarefas, threads daemon `alerta-email-`.
- Ao desligar, aguarda até 10 s as tarefas em andamento.
- Fila cheia → tarefa **descartada** e logada (`TaskRejectedException`).

### Limitações

- Fila **somente em memória**: reiniciar a aplicação pode perder alertas pendentes.
- **Sem retentativas**: falhas SMTP só são registradas em log.
- Entrega garantida exigiria uma outbox persistente.

## 3. Testes

- `src/test/resources/application.properties` desativa `auth` e `starttls` do SMTP padrão.
- `EmailAlertaServiceTest` valida o comportamento quando o SMTP não está configurado.
- `NotificacaoEstoqueServiceTest` verifica que o listener só enfileira o trabalho, sem aguardar o envio.

## 4. Segurança

- Nunca coloque credenciais no frontend nem no repositório; use variáveis de ambiente (`.env` fora do versionamento).
- Use senha de app do Gmail (exige verificação em duas etapas), não a senha da conta.
