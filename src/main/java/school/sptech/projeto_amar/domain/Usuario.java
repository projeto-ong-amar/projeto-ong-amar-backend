package school.sptech.projeto_amar.domain;

import jakarta.persistence.*;
import school.sptech.projeto_amar.domain.enums.TipoUsuario;

@Entity
public class Usuario {
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Id 
  private Integer id;
  private String nome;
  private String email;
  private String senha;

  @Enumerated(EnumType.STRING)
  private TipoUsuario tipoUsuario;
  private String status;
  private int versaoToken;

  public TipoUsuario getTipoUsuario() {
    return tipoUsuario;
  }

  public void setTipoUsuario(TipoUsuario tipoUsuario) {
    this.tipoUsuario = tipoUsuario;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getNome() {
    return nome;
  } 

  public void setNome(String nome) {
    this.nome = nome;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getSenha() {
    return senha;
  }

  public void setSenha(String senha) {
    this.senha = senha;
  }

  public int getVersaoToken() {
    return versaoToken;
  }

  public void setVersaoToken(int versaoToken) {
    this.versaoToken = versaoToken;
  }

}
