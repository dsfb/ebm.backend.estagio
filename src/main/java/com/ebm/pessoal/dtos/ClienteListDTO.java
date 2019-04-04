package com.ebm.pessoal.dtos;

import com.ebm.pessoal.domain.Cliente;

public class ClienteListDTO {
	
	private Integer id;
	private String nome;
	private String tipo;
	private Double limteCompra;
	public ClienteListDTO() {}
	public ClienteListDTO(Integer id, String nome, String tipo, double limite) {
		this.id=id;
		this.nome=nome;
		this.tipo = tipo;
		this.limteCompra = limite;
	}

	public ClienteListDTO(Cliente c) {
		this(c.getId(), c.getPessoa().getNome(), c.getPessoa().getTipo().getDescricao(), c.getLimite_compra().doubleValue());
	}
	public Double getLimteCompra() {
		return limteCompra;
	}
	public void setLimteCompra(Double limteCompra) {
		this.limteCompra = limteCompra;
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
	public String getTipo() {
		return tipo;
	}
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
}
