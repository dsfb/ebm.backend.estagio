package com.ebm.pessoal.repository;


import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.ebm.estoque.domain.CategoriaItem;
import com.ebm.pessoal.domain.Fornecedor;

@Repository	
public interface FornecedorRepository  extends JpaRepository<Fornecedor, Integer>{
	
	@Transactional(readOnly=true)
	Page<Fornecedor> findDistinctByCategoriasIn(Set<CategoriaItem> categorias, Pageable page);
	
	@Transactional(readOnly=true)
	List<Fornecedor> findDistinctByCategoriasIn(Set<CategoriaItem> categorias);

}
