package com.ebm.pessoal.service;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.ebm.exceptions.ObjectNotFoundException;
import com.ebm.pessoal.domain.Pessoa;
import com.ebm.pessoal.domain.PessoaFisica;
import com.ebm.pessoal.domain.PessoaJuridica;
import com.ebm.pessoal.domain.RG;
import com.ebm.pessoal.dtos.PessoaFisicaUpdateDTO;
import com.ebm.pessoal.dtos.PessoaJuridicaUpdateDTO;
import com.ebm.pessoal.repository.PessoaFisicaRepository;
import com.ebm.pessoal.repository.PessoaJuridicaRepository;

@Service
public class PessoaService {
	@Autowired
	private PessoaFisicaRepository pessoaFisicaRepository;
	@Autowired
	private PessoaJuridicaRepository pessoaJuridicaRepository;
	@Autowired
	private EnderecoService  enderecoService ;
	@Autowired
	private EmailService emailService;
	@Autowired
	private TelefoneService  telefoneService ;
	
	
	//insert
	@Transactional
	public PessoaFisica insert(PessoaFisica pf) {
		pf.setId(null);
		pf = pessoaFisicaRepository.save(pf);
		pf.setDataCadastro(LocalDateTime.now());
		saveAssociations(pf);
		return pf;
	}
	@Transactional
	public PessoaJuridica insert(PessoaJuridica pj) {
		pj.setId(null);
		pj.setDataCadastro(LocalDateTime.now());
		pj = pessoaJuridicaRepository.save(pj);
		saveAssociations(pj);
		return pj;
	}
	
	
	//update
	public PessoaFisica update(@Valid PessoaFisicaUpdateDTO pfDTO) {
		findPF(pfDTO.getId());
		PessoaFisica pf = fromDTO(pfDTO);
		pf.setDataUltimaModificacao(LocalDateTime.now());
		pf = pessoaFisicaRepository.save(pf);
		return pf;
	}
	public PessoaJuridica update(@Valid PessoaJuridicaUpdateDTO pjDTO) {
		findPJ(pjDTO.getId());
		PessoaJuridica pj = fromDTO(pjDTO);
		pj.setDataUltimaModificacao(LocalDateTime.now());
		pj = pessoaJuridicaRepository.save(pj);
		return pj;
	}
	
	
	//delete
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
		}catch(ObjectNotFoundException e ){
			findPJ(id);
			this.pessoaJuridicaRepository.deleteById(id);
			
		}
	}
	
	
	//find
	public PessoaFisica findPF(Integer id) {
		Optional<PessoaFisica> pf = pessoaFisicaRepository.findById(id);
		return pf.orElseThrow(() -> new ObjectNotFoundException("não foi possivel encontrar a pessoa de id: " + id));
	}
	public Page<PessoaFisica> findByNome(String nome, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaFisicaRepository.findAllByNomeLikeIgnoreCase(nome, pageRequest);
	}
	public PessoaFisica findbyCPF(String cpf ){
		return pessoaFisicaRepository.findOneByCpf(cpf).orElseThrow(
				()-> new ObjectNotFoundException("Não foi possivel encontrar uma pessoa com o cpf: "+ cpf));
	}
	public Page<PessoaFisica> findPFByEmail(String email, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaFisicaRepository.findAllByEmailLike(email, pageRequest);
	}
	public Page<PessoaFisica> findByRG(RG rg, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaFisicaRepository.findAllByRG(Example.of(rg), pageRequest);
				
	}
	
	public PessoaJuridica findPJ(Integer id) {
		Optional<PessoaJuridica> pj = pessoaJuridicaRepository.findById(id);
		return pj.orElseThrow(() -> new ObjectNotFoundException("não foi possivel encontrar a pessoa de id: " + id));
	}
	
	public Page<PessoaJuridica> findbyNomeFantasia(String nome, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaJuridicaRepository.findAllByNomeLikeIgnoreCase(nome,  pageRequest);
	}
	public PessoaJuridica findByCPNJ(String cnpj ){
		return pessoaJuridicaRepository.findOneByCnpj(cnpj).orElseThrow(
				()-> new ObjectNotFoundException("Não foi possivel encontrar uma pessoa com o cnpj: "+ cnpj));
	}
	public Page<PessoaJuridica> findPJByEmail(String email, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaJuridicaRepository.findAllByEmailLike(email, pageRequest);
	}
	public Page<PessoaJuridica> findByRazaoSocial(String razaoSocial, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaJuridicaRepository.findAllByRazaoSocialIgnoreCaseContaining(razaoSocial,  pageRequest);
	}
	public Page<PessoaJuridica> findByInscricaoEstadual(String inscricaoEstadual, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaJuridicaRepository.findAllByInscricaoEstadualIgnoreCaseContaining(inscricaoEstadual, pageRequest);
	}
	public Page<PessoaJuridica> findByInscricaoMunicipal(String inscricaoEstadual, Integer page, Integer linesPerPage, String orderBy, String direction){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return pessoaJuridicaRepository.findAllByInscricaoEstadualIgnoreCaseContaining(inscricaoEstadual, pageRequest);
	}
	
	
	
	//aux
	private void saveAssociations(Pessoa p) {
		
		p.setEmail(emailService.insertAll(p.getEmail())); 
		p.setEndereco(enderecoService.insertAll(p.getEndereco()));
		p.setTelefone(telefoneService.insertAll(p.getTelefone()));
	}
	private void getAssociationsFromBD(Pessoa p) {
		p.setEmail(emailService.findByPessoaId(p.getId()));
		p.setEndereco(enderecoService.findByPessoaId(p.getId()));
		p.setTelefone(telefoneService.findByPessoaId(p.getId()));
	}
	
	public PessoaJuridica fromDTO(@Valid PessoaJuridicaUpdateDTO dto) {
		PessoaJuridica pj = new PessoaJuridica(dto.getId(), dto.getNome(), dto.getCnpj(), dto.getRazaoSocial(), dto.getInscricaoEstadual(), dto.getInscricaoMunicipal());
		getAssociationsFromBD(pj);
		return pj;
	}
	public PessoaFisica fromDTO(@Valid PessoaFisicaUpdateDTO dto) {
		PessoaFisica pf = new PessoaFisica(dto.getId(), dto.getNome(), dto.getCpf(), dto.getDataNascimento(), dto.getRG(), dto.getNacionalidade(), dto.getNaturalidade());
		getAssociationsFromBD(pf);
		return pf;
	}
	
}
