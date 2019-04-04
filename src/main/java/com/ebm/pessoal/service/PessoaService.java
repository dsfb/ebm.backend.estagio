package com.ebm.pessoal.service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Service;

import com.ebm.exceptions.DataIntegrityException;
import com.ebm.exceptions.ObjectNotFoundException;
import com.ebm.pessoal.domain.Email;
import com.ebm.pessoal.domain.Endereco;
import com.ebm.pessoal.domain.Pessoa;
import com.ebm.pessoal.domain.PessoaFisica;
import com.ebm.pessoal.domain.PessoaJuridica;
import com.ebm.pessoal.domain.RG;
import com.ebm.pessoal.domain.Telefone;
import com.ebm.pessoal.domain.TipoPessoa;
import com.ebm.pessoal.repository.PessoaFisicaRepository;
import com.ebm.pessoal.repository.PessoaJuridicaRepository;

import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;
import br.com.caelum.stella.validation.InvalidStateException;

@Service
public class PessoaService {
	@Autowired
	private PessoaFisicaRepository pessoaFisicaRepository;
	@Autowired
	private PessoaJuridicaRepository pessoaJuridicaRepository;
	@Autowired
	private EnderecoService enderecoService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private TelefoneService telefoneService;
	

	// insert --------------------------------------------------------------------------------------------------------
	@Transactional
	public PessoaFisica insert(PessoaFisica pf) {	
		validateCPF(pf.getCpf());
		
		try{
			findbyCPF(pf.getCpf());
			throw new DataIntegrityException("Ja existe uma pessoa com esse cpf, se você deseja alterar dados, modifique a pessoa ja existente");
		}catch(ObjectNotFoundException e) {
			pf.setId(null);
			pf.setDataCadastro(LocalDateTime.now());
			saveAssociations(pf);
			pf = pessoaFisicaRepository.save(pf);
			
			return pf;	
		}
		
	}

	@Transactional
	public PessoaJuridica insert(PessoaJuridica pj) {
		pj.setId(null);
		validateCNPJ(pj.getCnpj());
		pj.setDataCadastro(LocalDateTime.now());
		pj = pessoaJuridicaRepository.save(pj);
		saveAssociations(pj);
		return pj;
	}


	public Pessoa insert(Pessoa pessoa) {
		return pessoa.getTipo() == TipoPessoa.PESSOAFISICA ? insert((PessoaFisica) pessoa) : insert((PessoaJuridica) pessoa);
		
	}
	// update --------------------------------------------------------------------------------------------------------
	public Pessoa update(Pessoa pessoa) {
		return pessoa.getTipo() == TipoPessoa.PESSOAFISICA ? update((PessoaFisica) pessoa) : update((PessoaJuridica) pessoa);
		
	}
	public PessoaFisica update(PessoaFisica pf) {
		findPF(pf.getId());
		validateCPF(pf.getCpf());
		saveAssociations(pf);
		pf.setDataUltimaModificacao(LocalDateTime.now());
		pf = pessoaFisicaRepository.save(pf);
		return pf;
	}

	public PessoaJuridica update( PessoaJuridica pj) {
		findPJ(pj.getId());
		validateCNPJ(pj.getCnpj());
		saveAssociations(pj);
		pj.setDataUltimaModificacao(LocalDateTime.now());
		pj = pessoaJuridicaRepository.save(pj);
		return pj;
	}

	// delete --------------------------------------------------------------------------------------------------------
	public void delete(PessoaFisica pf) {
		findPF(pf.getId());
		pessoaFisicaRepository.delete(pf);
	}

	public void delete(PessoaJuridica pj) {
		findPJ(pj.getId());
		pessoaJuridicaRepository.delete(pj);
	}

	public void deleteById(Integer id) {
		try {
			findPF(id);
			pessoaFisicaRepository.deleteById(id);
		} catch (ObjectNotFoundException e) {
			findPJ(id);
			this.pessoaJuridicaRepository.deleteById(id);

		}
	}

	// find --------------------------------------------------------------------------------------------------------
	public Pessoa findById(Integer id) {
		Optional<PessoaFisica> pf =  pessoaFisicaRepository.findById(id);
		
		if(pf.isPresent()) 
			return pf.get();
		
		return pessoaJuridicaRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException("não foi possivel encontrar a pessoa de id: " + id));
	}
	
	public PessoaFisica findPF(Integer id) {
		Optional<PessoaFisica> pf = pessoaFisicaRepository.findById(id);
		return pf.orElseThrow(() -> new ObjectNotFoundException("não foi possivel encontrar a pessoa de id: " + id));
	}

	public List<PessoaFisica> findByNome(String nome) {
		return pessoaFisicaRepository.findAllByNomeLikeIgnoreCase(nome);
	}
	public PessoaFisica findbyCPF(String cpf) {
		return pessoaFisicaRepository.findOneByCpf(cpf).orElseThrow(
				() -> new ObjectNotFoundException("Não foi possivel encontrar uma pessoa com o cpf: " + cpf));
	}

	public List<PessoaFisica> findPFByEmail(String email){
		return pessoaFisicaRepository.findAllByEmailLike(email);
	}

	public List<PessoaFisica> findByRG(RG rg) {
		return pessoaFisicaRepository.findAllByRG(Example.of(rg));

	}

	public PessoaJuridica findPJ(Integer id) {
		Optional<PessoaJuridica> pj = pessoaJuridicaRepository.findById(id);
		return pj.orElseThrow(() -> new ObjectNotFoundException("não foi possivel encontrar a pessoa de id: " + id));
	}


	public PessoaJuridica findByCPNJ(String cnpj) {
		return pessoaJuridicaRepository.findOneByCnpj(cnpj).orElseThrow(
				() -> new ObjectNotFoundException("Não foi possivel encontrar uma pessoa com o cnpj: " + cnpj));
	}

	public List<PessoaJuridica> findPJByEmail(String email) {
		 
		return pessoaJuridicaRepository.findAllByEmailLike(email);
	}

	public List<PessoaJuridica> findbyNomeFantasia(String nome) {
		return pessoaJuridicaRepository.findAllByNomeLikeIgnoreCase(nome);	
	}
	public List<PessoaJuridica> findByRazaoSocial(String nome) {
		return pessoaJuridicaRepository.findAllByRazaoSocialIgnoreCaseContaining(nome);
	}
	public List<PessoaJuridica> findByInscricaoEstadual(String inscricaoEstadual) {
		 
		return pessoaJuridicaRepository.findAllByInscricaoEstadualIgnoreCaseContaining(inscricaoEstadual);
	}
	public List<PessoaJuridica> findByInscricaoMunicipal(String inscricaoEstadual, Integer page, Integer linesPerPage,
			String orderBy, String direction) { 
		return pessoaJuridicaRepository.findAllByInscricaoEstadualIgnoreCaseContaining(inscricaoEstadual);
	}
	
	public List<? extends Pessoa> findByTipo(TipoPessoa tipo) {
		if(tipo == TipoPessoa.PESSOAFISICA)
			return pessoaFisicaRepository.findAll();
		
		return pessoaJuridicaRepository.findAll();
	}
	public Set<Integer> getPessoaIdBy(String tipo, String nome, String nomeFantasia, String razaoSocial) {
		Set<Integer> ids = new HashSet<>();
		
		if(tipo.equals(TipoPessoa.PESSOAFISICA.getDescricao()) ){
			ids.addAll(pessoaFisicaRepository.findIdOfAll());
			if(!nome.equals(""))
			ids.addAll(pessoaFisicaRepository.findIdOfAllWithNameContain(nome));
		}else if(tipo.equals(TipoPessoa.PESSOAJURIDICA.getDescricao())) {
			if(!nomeFantasia.equals(""))
				ids.addAll(pessoaJuridicaRepository.findIdOfAllWithNameContain(nomeFantasia));
			if(!razaoSocial.equals(""))
				ids.addAll(pessoaJuridicaRepository.findIdOfAllWithRazaoSocialContain(razaoSocial));
		}
		return ids;
	}

	// aux --------------------------------------------------------------------------------------------------------
	private void saveAssociations(Pessoa p) {
		Optional<List<Email>> emails = Optional.of(p.getEmail());
		Optional<List<Endereco>> enderecos = Optional.of(p.getEndereco());
		Optional<List<Telefone>> telefones = Optional.of(p.getTelefone());
		enderecos.ifPresent( o ->  p.setEndereco(o.stream().map( e -> e.getId() == null ? enderecoService.insert(e) : enderecoService.update(e)).collect(Collectors.toList()))   );
		emails.ifPresent( o -> p.setEmail(o.stream().map( e -> e.getId() == null ? emailService.insert(e) : emailService.update(e)).collect(Collectors.toList())));
		telefones.ifPresent(o -> p.setTelefone(o.stream().map( t -> t.getId() == null ? telefoneService.insert(t) : telefoneService.update(t)).collect(Collectors.toList())));
		
	}
	
	public void validateCPF(String cpf) {
		try{
			CPFValidator cpfValidator = new CPFValidator();
			cpfValidator.assertValid(cpf);
		}catch(InvalidStateException e) {
			throw new DataIntegrityException("Não foi possivel validar esse cpf, tente novamente com um cpf valido");
		}
	}
	public void validateCNPJ(String cnpj) {
		try{
			CNPJValidator cnpjValidator = new CNPJValidator();
			cnpjValidator.assertValid(cnpj);
		}catch(InvalidStateException e) {
			throw new DataIntegrityException("Não foi possivel validar esse cnpj, tente novamente com um cnpj valido");
		}
	}

	public Integer findByCpfOrCnpj(String cpf, String cnpj) {
		try{
			return  findbyCPF(cpf).getId();
		} catch(ObjectNotFoundException e ) {
			return findByCPNJ(cnpj).getId();
		}
	}

}
