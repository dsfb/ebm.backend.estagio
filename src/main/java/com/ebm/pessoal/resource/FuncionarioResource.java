package com.ebm.pessoal.resource;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ebm.exceptions.DataIntegrityException;
import com.ebm.pessoal.domain.Funcionario;
import com.ebm.pessoal.dtos.FuncionarioListDTO;
import com.ebm.pessoal.service.FuncionarioService;


@RestController
@RequestMapping(value = "/funcionarios")
public class FuncionarioResource {
	@Autowired
	private FuncionarioService funcionarioService;	
	
	@PostMapping
	public ResponseEntity<Void> insert( @RequestBody Funcionario funcionario){
		Funcionario obj = funcionarioService.insert(funcionario);

		URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
				.buildAndExpand(obj.getId()).toUri();
		return ResponseEntity.created(uri).build();
	}
	
	@PutMapping(value = "/{id}")
	public ResponseEntity<Void> update(@RequestBody Funcionario funcionario, @PathVariable Integer id){
		funcionario.setId(id);
		funcionario = funcionarioService.update(funcionario);
		return ResponseEntity.noContent().build();
		
	}
	
	@DeleteMapping(value = "/{id}")
	public ResponseEntity<Void> delete(@PathVariable Integer id) {
		funcionarioService.delete(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping(value="/{id}")
	public ResponseEntity<Funcionario> find(@PathVariable Integer id) {
		Funcionario obj = funcionarioService.findById(id);
		return ResponseEntity.ok(obj);
	}
	
	@GetMapping
	public ResponseEntity<Funcionario> findBy( 
			@RequestParam(value ="cpf", defaultValue="", required = false) final String cpf,
			@RequestParam(value = "cnpj", defaultValue="", required = false) final String cnpj){
		Funcionario funcionario;
		if(!cpf.equals("") || !cnpj.equals("")) 
			funcionario = funcionarioService.findByCpfOrCnpj(cpf, cnpj);
		 else 
			throw new DataIntegrityException("Não foi passado dados");
		
		return ResponseEntity.ok(funcionario);
	}

	@GetMapping(value="/page")
	public ResponseEntity<Page<FuncionarioListDTO>> findAllBy(
			@RequestParam(value ="nome", defaultValue="") String nome,
			@RequestParam(value ="tipo", defaultValue="") String tipo,
			@RequestParam(value ="nomeFantasia", defaultValue="") String nomeFantasia,
			@RequestParam(value ="razaoSocial", defaultValue="") String razaoSocial,
			@RequestParam(value ="cargo", defaultValue="") String cargo,
			@RequestParam(value ="page", defaultValue="0") Integer page,
			@RequestParam(value ="linesPerPage", defaultValue="10")Integer linesPerPage,
			@RequestParam(value ="orderBy", defaultValue="nome")String orderBy,
			@RequestParam(value ="direction", defaultValue="ASC")String direction ) {
		
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		Page<FuncionarioListDTO> rs = funcionarioService.findBy(tipo, nome, nomeFantasia, razaoSocial, cargo, pageRequest);
		return ResponseEntity.ok().body(rs);
	}
	
	
}
