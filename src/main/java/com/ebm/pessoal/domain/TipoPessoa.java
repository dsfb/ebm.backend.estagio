package com.ebm.pessoal.domain;

public enum TipoPessoa {
	PESSOAFISICA(0,"Pessoa Fisica"),
	PESSOAJURIDICA(1, "Pessoa Juridica");
	
	private int cod;
	private String desc;
	
    TipoPessoa(int cod, String desc) {
		this.cod = cod;
		this.desc = desc;
	}
	public int getCod() {
		return cod;
	}
	public String getDescricao() {
		return desc;
	}
	
	public static TipoPessoa toEnum(Integer cod) {
		if(cod == null) {
			return null;
		}
		for(TipoPessoa x: TipoPessoa.values()) {
			if(cod.equals(x.getCod()))
				return x;
		}
		
		throw new IllegalArgumentException("id invalido: " + cod);
	}
}
