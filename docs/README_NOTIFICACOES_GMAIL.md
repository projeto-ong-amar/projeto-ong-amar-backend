# Guia: SMTP Gmail e notificações por e-mail

Este guia explica como usar uma conta Gmail pessoal para enviar gratuitamente os alertas de estoque da aplicação. O SMTP dos alertas tem configuração própria; a recuperação de senha continua usando as variáveis `MAIL_*` atuais, sem mudança no `EmailService`.

## 1. Preparar a conta Gmail

1. Escolha uma conta Gmail que será usada como remetente dos alertas.
2. Ative a **Verificação em duas etapas** nas configurações da Conta Google.
3. Acesse a página de [senhas de app](https://myaccount.google.com/apppasswords) da Conta Google.
4. Crie uma senha de app para a aplicação. Copie o valor gerado e guarde-o como segredo.
5. Use o endereço dessa conta como remetente. Por exemplo, se a conta for `alertas.projeto@gmail.com`, use esse mesmo endereço em `ALERT_MAIL_FROM`.

Senhas de app só aparecem em contas elegíveis com a verificação em duas etapas ativa. Algumas contas gerenciadas por organizações ou com proteções adicionais podem não oferecer esse recurso. Use a senha de app, **nunca a senha normal da conta Google**. Se o valor for exibido com espaços, remova-os ao configurar a variável.

## 2. Configurar o SMTP dos alertas

Defina as variáveis de ambiente no processo do **backend**. Para Gmail pessoal, use:

| Variável | Valor |
|---|---|
| `ALERT_MAIL_HOST` | `smtp.gmail.com` |
| `ALERT_MAIL_PORT` | `587` |
| `ALERT_MAIL_USERNAME` | Endereço completo da conta Gmail remetente |
| `ALERT_MAIL_PASSWORD` | Senha de app gerada na Conta Google |
| `ALERT_MAIL_FROM` | Mesmo endereço Gmail usado em `ALERT_MAIL_USERNAME` |
| `ALERTS_EXPIRY_CRON` | Opcional; padrão `0 0 8 * * *` (diariamente às 08:00) |
| `ALERTS_TIME_ZONE` | Opcional; padrão `America/Sao_Paulo` |

### Linux ou macOS — execução local

No terminal, dentro de `projeto-ong-amar-backend`:

```bash
export ALERT_MAIL_HOST='smtp.gmail.com'
export ALERT_MAIL_PORT='587'
export ALERT_MAIL_USERNAME='alertas.projeto@gmail.com'
export ALERT_MAIL_PASSWORD='senha_de_app_sem_espacos'
export ALERT_MAIL_FROM='alertas.projeto@gmail.com'
export ALERTS_EXPIRY_CRON='0 0 8 * * *'
export ALERTS_TIME_ZONE='America/Sao_Paulo'
./mvnw spring-boot:run
```

### Windows PowerShell — execução local

```powershell
$env:ALERT_MAIL_HOST = 'smtp.gmail.com'
$env:ALERT_MAIL_PORT = '587'
$env:ALERT_MAIL_USERNAME = 'alertas.projeto@gmail.com'
$env:ALERT_MAIL_PASSWORD = 'senha_de_app_sem_espacos'
$env:ALERT_MAIL_FROM = 'alertas.projeto@gmail.com'
$env:ALERTS_EXPIRY_CRON = '0 0 8 * * *'
$env:ALERTS_TIME_ZONE = 'America/Sao_Paulo'
.\mvnw.cmd spring-boot:run
```

Essas variáveis ficam disponíveis para o processo iniciado naquele terminal. Não salve a senha de app em arquivos versionados, no frontend ou no `localStorage`.

### Produção

No painel da plataforma que hospeda o backend, cadastre as variáveis `ALERT_MAIL_*` como secrets/variáveis de ambiente e reinicie ou faça novo deploy. **Não** substitua as variáveis `MAIL_*` existentes: elas continuam sendo usadas para a recuperação de senha. Caso o deploy use um único Gmail para os dois fluxos, configure-o deliberadamente nos dois grupos de variáveis.

O backend ainda precisa das demais configurações já existentes (`DB_*`, `JWT_*`, `SECURITY_PEPPER` e as variáveis de SMTP `MAIL_*` para recuperação de senha).

## 3. Configurar quem recebe os alertas

1. Inicie o backend e o frontend e entre na aplicação com seu usuário.
2. Abra **Configurações → Notificações e Alertas**.
3. Em **Notificações por E-mail**, habilite os tipos desejados: estoque baixo, risco de vencimento, novas entradas e/ou novas saídas.
4. Preencha **E-mail para Notificação** com o endereço destinatário. Ele pode ser Gmail ou outro provedor.
5. Clique em **Salvar Notificações**.

As preferências e o destinatário são individuais por usuário e ficam salvos no backend. As opções aparecem habilitadas por padrão para uma preferência nova, mas o endereço começa vazio; informe-o e salve para começar a receber alertas. Somente usuários ativos com a opção correspondente habilitada e um destinatário preenchido recebem aquele tipo de mensagem. Se usuários diferentes escolherem o mesmo endereço, o sistema evita duplicar esse destinatário no mesmo envio.

## 4. Configurar limites de estoque e vencimento

Em **Configurações → Métricas de Estoque**, ajuste os valores e clique em **Salvar Métricas**:

- **Estoque Baixo**: limite mínimo por produto. O alerta é enviado quando o estoque cruza de igual/acima do limite para abaixo dele.
- **Risco de Vencimento**: número de dias considerado no resumo diário de lotes próximos do vencimento.

Essas métricas são globais do sistema. Os limites iniciais são 10 unidades e 30 dias. As preferências para receber cada alerta continuam sendo individuais.

## 5. Gatilhos dos e-mails

| Notificação | Quando é enviada |
|---|---|
| Nova entrada | Após registrar com sucesso uma nova movimentação `ENTRADA`. Editar uma entrada não gera outro e-mail de nova entrada. |
| Nova saída | Após registrar com sucesso uma nova movimentação `SAIDA`. Editar uma saída não gera outro e-mail de nova saída. |
| Estoque baixo | Após uma alteração confirmada cruzar o limite configurado para baixo. Entradas, saídas, perdas, edições e remoções podem ser avaliadas. |
| Risco de vencimento | Resumo diário de lotes ativos, com saldo positivo e validade entre hoje e hoje + o período configurado, inclusive. |

`PERDA` é um tipo diferente de movimentação: não envia o e-mail de nova saída, mas reduz o estoque e pode disparar o alerta de estoque baixo. O resumo de vencimento pode repetir diariamente enquanto existirem lotes no período. O servidor deve estar em execução no horário agendado; se estiver desligado, não há envio retroativo.

Os alertas são enfileirados após o commit do banco e enviados em segundo plano. Assim, a resposta da ação de estoque não espera o SMTP e a interface encerra “Salvando...” assim que o backend confirma a gravação.

## 6. Testar em ambiente de desenvolvimento

Use um banco local/de teste e destinatários que você controla.

1. Configure as variáveis `ALERT_MAIL_*`, reinicie o backend e salve seu endereço e preferências na tela.
2. Registre uma entrada e verifique o e-mail, se novas entradas estiver habilitado.
3. Registre uma saída e verifique o e-mail, se novas saídas estiver habilitado.
4. Para testar estoque baixo, escolha um limite e registre uma saída ou perda que leve o produto de igual/acima para abaixo desse limite. Alterar somente o limite não dispara o alerta.
5. Para testar o resumo de vencimento sem esperar até as 08:00, defina temporariamente `ALERTS_EXPIRY_CRON='0 * * * * *'` no ambiente local, crie um lote elegível e aguarde a próxima execução. Essa expressão executa a cada minuto e pode repetir o resumo; use apenas com destinatário de teste.
6. Restaure `ALERTS_EXPIRY_CRON='0 0 8 * * *'` e reinicie o backend.
7. Desabilite um tipo de notificação ou remova o endereço e confirme que esse usuário não recebe mais aquele alerta.

Não há um botão de “enviar e-mail de teste” na aplicação. O teste dos gatilhos é feito com operações reais de estoque em desenvolvimento.

## 7. Limites e diagnóstico

- Gmail pessoal tem limites de envio e políticas contra abuso; não use essa configuração para campanhas ou alto volume. O Google pode bloquear temporariamente envios que pareçam suspeitos ou excedam os limites da conta.
- Se a opção de senha de app não aparecer, confirme a verificação em duas etapas e se a conta permite senhas de app.
- Se nenhum e-mail chegar, confira se `ALERT_MAIL_USERNAME`, `ALERT_MAIL_PASSWORD` e `ALERT_MAIL_FROM` estão definidos no **processo do backend** e se o remetente é a conta autenticada.
- Se a tela confirmar o salvamento, isso confirma somente as preferências; não envia mensagem de teste. Faça a operação correspondente para gerar o alerta.
- Estoque baixo exige uma transição de igual/acima do limite para abaixo. Se já estava abaixo antes da operação, não há novo cruzamento.
- Falhas SMTP ficam registradas no log e não desfazem a movimentação. A fila é somente em memória, limitada a 100 tarefas, e não há retentativas: se estiver cheia, a tarefa é descartada e registrada no log; se o processo reiniciar antes do envio, tarefas pendentes podem se perder. Uma outbox persistente seria necessária para entrega garantida.

Consulte também a [página de senhas de app do Google](https://support.google.com/mail/answer/185833) e nunca imprima ou compartilhe o valor de `ALERT_MAIL_PASSWORD`.
