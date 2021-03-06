package com.ebm.pessoal.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.validator.constraints.Length;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Inheritance(strategy = InheritanceType.JOINED)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "tipo")
public abstract class Pessoa implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "pessoa_id")
	@EqualsAndHashCode.Include
	private Integer id;

	@Column(length = 60, nullable = false)
	@NotNull(message = "O campo nome não pode ser nulo")
	@NotEmpty(message = "O campo nome não pode ser vazio")
	@Length(min = 3, max = 60, message = "O campo nome deve possuir entre 3 e 60 caracteres")
	protected String nome;
	@Embedded
	private HistoricoCadastral historico = new HistoricoCadastral();
	@OneToMany
	@JoinColumn(name = "pessoa_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	@NotNull(message = "O campo email não pode ser nulo")
	@NotEmpty(message = "O campo email não pode ser vazio")
	@Valid
	private List<Email> email = new ArrayList<Email>();
	@OneToMany
	@JoinColumn(name = "pessoa_id")
	@NotNull(message = "O campo telefone não pode ser nulo")
	@NotEmpty(message = "O campo telefone não pode ser vazio")
	@Valid
	@LazyCollection(LazyCollectionOption.FALSE)
	private List<Telefone> telefone = new ArrayList<Telefone>();
	
	@OneToMany
	@JoinColumn(name = "pessoa_id")
	@LazyCollection(LazyCollectionOption.FALSE)
	@NotNull(message = "O campo endereco não pode ser nulo")
	@NotEmpty(message = "O campo endereco não pode ser vazio")
	@Valid
	private List<Endereco> endereco = new ArrayList<Endereco>();
	@Enumerated(EnumType.STRING)
	@NotNull(message = "O campo tipo não pode ser nulo")
	private TipoPessoa tipo;

	public Pessoa(Integer id, String nome, TipoPessoa tipo) {
		super();
		this.id = id;
		this.nome = nome;
		this.tipo = tipo;
	}

	@Transient
	@JsonIgnore
	public Email getEmailPrincipal() {
		return email.stream().filter(e -> e.isPrincipal()).findAny().get();
	}

	@Transient
	@JsonIgnore
	public Telefone getTelefonePrincipal() {
		return telefone.stream().filter(e -> e.isPrincipal()).findAny().get();
	}

	@Transient
	@JsonIgnore
	public Endereco getEnderecoPrincipal() {
		return endereco.stream().filter(e -> e.isPrincipal()).findAny().get();
	}

	@JsonIgnore
	public abstract String getDocument();

}
