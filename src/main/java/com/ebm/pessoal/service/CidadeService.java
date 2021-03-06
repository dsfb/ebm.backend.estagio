package com.ebm.pessoal.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ebm.geral.exceptions.DataIntegrityException;
import com.ebm.geral.exceptions.ObjectNotFoundException;
import com.ebm.geral.utils.Utils;
import com.ebm.pessoal.domain.Cidade;
import com.ebm.pessoal.domain.Estado;
import com.ebm.pessoal.repository.CidadeRepository;

@Service
public class CidadeService {

	public static final String CIDADE_ESTADO_INVALIDO = DataIntegrityException.DEFAULT
			+ " cidade esta atrelada a um estado valido";
	public static final String CIDADE_NOTFOUND_ID = ObjectNotFoundException.DEFAULT + " uma cidade com id: ";
	public static final String CIDADE_NOTFOUND_UF = ObjectNotFoundException.DEFAULT + " cidades para o UF ";

	@Autowired
	private CidadeRepository cidadeRepository;
	@Autowired
	private EstadoService estadoService;

	@Transactional
	public Cidade save(Cidade cidade) {
		
		if (cidade.getEstado() == null) {
			throw new DataIntegrityException(CIDADE_ESTADO_INVALIDO);
		}
		Optional<Cidade> result = cidadeRepository.findOneByNomeAndEstado(cidade.getNome(), cidade.getEstado().getUf());

		if (result.isPresent())
			cidade.setId(result.get().getId());

		cidade.setEstado(estadoService.save(cidade.getEstado()));
		Utils.audita(cidade.getHistorico());
		return cidadeRepository.save(cidade);

	}

	public int count() {
		return (int) cidadeRepository.count();
	}

	public void deleteAll(Boolean propagaDeletacao) {

		cidadeRepository.deleteAll();
		if (propagaDeletacao)
			estadoService.deleteAll(true);
	}

	public void delete(Cidade cidade) {
		cidadeRepository.delete(cidade);
		try {
			estadoService.delete(cidade.getEstado().getId());
		} catch (DataIntegrityException e) {
		}

	}

	public Cidade findById(Integer id) {

		return cidadeRepository.findById(id).orElseThrow(() -> new ObjectNotFoundException(CIDADE_NOTFOUND_ID + id));
	}

	public boolean exist(Estado estado) {

		return cidadeRepository.existsByEstado(estado);
	}

	@Transactional
	public List<Cidade> saveAll(List<Cidade> cidades) {
		return cidades.stream().map(c -> save(c)).collect(Collectors.toList());

	}

	public List<Cidade> findByEstado(String uf) {
		Estado estado = estadoService.findByUf(uf);

		List<Cidade> cidades = cidadeRepository.findAllByEstado(estado);
		if (cidades == null || cidades.size() == 0)
			throw new ObjectNotFoundException(CIDADE_NOTFOUND_UF + uf);
		return cidades;

	}

}
