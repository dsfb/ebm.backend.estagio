package com.ebm.security.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;

import javax.transaction.Transactional;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ebm.BaseTest;
import com.ebm.geral.domain.RestResponsePage;
import com.ebm.geral.resource.exception.ValidationError;
import com.ebm.geral.service.PopulaBD;
import com.ebm.geral.utils.Utils;
import com.ebm.pessoal.domain.Funcionario;
import com.ebm.pessoal.domain.TipoPessoa;
import com.ebm.security.Usuario;
import com.ebm.security.dto.UsuarioListDTO;
import com.ebm.security.dto.UsuarioNewDTO;
import com.ebm.security.dto.UsuarioUpdateDTO;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;

public class UsuarioResourceTest extends BaseTest {
	@Autowired
	private PopulaBD bd;

	private final String ENDPOINT_BASE = "/usuarios";
	private final String BASE_AUTHORITY = "USUARIO_";

	@Before
	public void setUp() {
		bd.instanciaFuncionario(true).instanciaUsuarios();
		this.bd.getCargoS().save(bd.funf1.getCargo());
		this.bd.getCargoS().save(bd.funj1.getCargo());

		this.bd.getPessoaS().save(bd.funf1.getPessoa());
		this.bd.getPessoaS().save(bd.funf2.getPessoa());
		this.bd.getPessoaS().save(bd.funj1.getPessoa());
		this.bd.getFuncionarioS().save(bd.funf1);
		this.bd.getFuncionarioS().save(bd.funf2);
		this.bd.getFuncionarioS().save(bd.funj1);
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testInsercaoAllNulo() throws Exception {
		UsuarioNewDTO user = UsuarioNewDTO.from(this.bd.user1);
		user.setLogin(null);
		user.setSenha(null);
		user.setPermissoes(null);
		user.setFuncionario_id(null);

		this.util.testPost(ENDPOINT_BASE, user, status().isUnprocessableEntity()).andDo(print())
				.andDo(resultRequest -> {
					ValidationError error = util.getValidationErrorOf(resultRequest);
					assertTrue(error.getErrors().size() == 5);
					error.getErrors().stream()
							.allMatch(err -> Arrays.asList("login", "senha", "permissoes", "funcionario_id").stream()
									.anyMatch(fi -> err.getFieldName().equals(fi)));
				});
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testInsercaoAllEmptyAndLength() throws Exception {
		UsuarioNewDTO user = UsuarioNewDTO.from(this.bd.user1);
		user.setLogin("");
		user.setSenha("");
		user.setPermissoes(new HashSet<>());

		this.util.testPost(ENDPOINT_BASE, user, status().isUnprocessableEntity()).andDo(print())
				.andDo(resultRequest -> {
					ValidationError error = util.getValidationErrorOf(resultRequest);
					assertTrue(error.getErrors().size() == 5);
					error.getErrors().stream().allMatch(err -> Arrays.asList("login", "senha", "permissoes").stream()
							.anyMatch(fi -> err.getFieldName().equals(fi)));
				});
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testValidacaoInsercao() throws Exception {
		UsuarioNewDTO user = UsuarioNewDTO.from(this.bd.user1);
		util.testPostExpectCreated(ENDPOINT_BASE, user);
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testValidacaoUsuarioMaxLengths() throws Exception {
		UsuarioNewDTO user = UsuarioNewDTO.from(this.bd.user1);
		user.setLogin(Utils.getRandomString(21));
		user.setSenha(Utils.getRandomString(21));

		util.testPost(ENDPOINT_BASE, user, status().isUnprocessableEntity()).andDo(requestResult -> {
			ValidationError errors = util.getValidationErrorOf(requestResult);
			assertTrue(errors.getErrors().size() == 2);
			errors.getErrors().stream().allMatch(
					err -> Arrays.asList("login", "senha").stream().anyMatch(fi -> err.getFieldName().equals(fi)));
		});

	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POSsT" })
	public void testInsersaoSemAthority() throws Exception {

		this.util.testPostExpectForbidden(ENDPOINT_BASE, UsuarioNewDTO.from(bd.user1));
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testInsercaoUsuarioComPessoaQueJaPertenceAOutroUsuario() throws Exception {
		UsuarioNewDTO user1 = UsuarioNewDTO.from(bd.user1);
		UsuarioNewDTO user2 = UsuarioNewDTO.from(bd.user2);
		bd.getUsuarioService().save(user2);
		user1.setFuncionario_id(user2.getFuncionario_id());
		this.util.testPost(ENDPOINT_BASE, user1, status().isBadRequest());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "PUT",
			BASE_AUTHORITY + "POST" })
	public void testUpdateSemMudarPessoa() throws Exception {
		UsuarioNewDTO user1 = UsuarioNewDTO.from(bd.user1);
		this.util.testPostExpectCreated(ENDPOINT_BASE, user1).andDo(result -> {
			Integer id = this.util.getIdRedirect(result, ENDPOINT_BASE);

			UsuarioUpdateDTO user2 = UsuarioUpdateDTO.from(this.bd.getUsuarioService().findById(id));
			user2.setLogin("novoLogin");
			this.util.testPutExpectNoContent(ENDPOINT_BASE + "/" + id, user2);
			this.bd.user1 = this.bd.getUsuarioService().findById(id);
			assertThat(this.bd.user1.getLogin(), equalTo("novoLogin"));

		});

	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testaFindById() throws Exception {
		this.bd.user1 = this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user1));
		util.testGetExpectedSucess(this.ENDPOINT_BASE, bd.user1.getId());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testaFindByIdExpectNotFound() throws Exception {
		util.testGet(this.ENDPOINT_BASE, 1, status().isNotFound());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GETT" })
	public void testaFindByIdNoAuthority() throws Exception {
		this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user1));
		util.testGet(this.ENDPOINT_BASE, 1, status().isForbidden());
	}

	//
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindCnpj() throws Exception {
		this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user5));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user5.getFuncionario().getPessoa().getDocument());

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isOk()).andDo(result -> {
			Usuario func_resut = this.util.objectMapper().readValue(result.getResponse().getContentAsString(),
					Usuario.class);
			assertTrue(func_resut.getId().equals(this.bd.user5.getFuncionario().getPessoa().getId()));
		});
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindCnpjInvalido() throws Exception {
		this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user5));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user5.getFuncionario().getPessoa().getDocument() + "1");

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isBadRequest());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindCnpjNaoExiste() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user5.getFuncionario().getPessoa().getDocument());

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isNotFound());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindCPF() throws Exception {
		this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user1));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user1.getFuncionario().getPessoa().getDocument());

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isOk()).andDo(result -> {
			Usuario func_resut = this.util.objectMapper().readValue(result.getResponse().getContentAsString(),
					Usuario.class);
			assertTrue(func_resut.getId().equals(this.bd.user1.getFuncionario().getPessoa().getId()));
		});
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GeET" })
	public void testFindCPFSemPermissao() throws Exception {
		this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user1));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user1.getFuncionario().getPessoa().getDocument());

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isForbidden());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindCPFInvalido() throws Exception {
		this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user1));
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user1.getFuncionario().getPessoa().getDocument() + "1");

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isBadRequest());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindCPFNaoExiste() throws Exception {
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("value", this.bd.user1.getFuncionario().getPessoa().getDocument());

		this.util.testGetRequestParams(ENDPOINT_BASE + "/document", params, status().isNotFound());
	}

	private RestResponsePage<UsuarioListDTO> getPage(MvcResult result)
			throws UnsupportedEncodingException, IOException, JsonParseException, JsonMappingException {
		String content = result.getResponse().getContentAsString();
		RestResponsePage<UsuarioListDTO> responseList = this.util.objectMapper().readValue(content,
				new TypeReference<RestResponsePage<UsuarioListDTO>>() {
				});
		return responseList;
	}

//
	private void prepara() {
		this.bd.getCargoS().save(bd.funf3.getCargo());
		this.bd.getCargoS().save(bd.funf4.getCargo());
		bd.funf4.getPessoa().getEmail().add(Utils.getRandomEmail(bd.funf4.getPessoa(), false));
		this.bd.getPessoaS().save(bd.funf3.getPessoa());
		this.bd.getPessoaS().save(bd.funf4.getPessoa());
		this.bd.getFuncionarioS().save(bd.funf3);
		this.bd.getFuncionarioS().save(bd.funf4);
		this.bd.getUsuarioService().saveAll(Arrays.asList(bd.user1, bd.user2, bd.user3, bd.user4));

	}

	private void testSePossuITodos(Page<UsuarioListDTO> list) {
		assertThat(list.getNumberOfElements(), equalTo(4));
		assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf1.getPessoa().getNome())));
		assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf2.getPessoa().getNome())));
		assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf3.getPessoa().getNome())));
		assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf4.getPessoa().getNome())));
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindByAllNull() throws Exception {
		prepara();

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		util.testGetPage(ENDPOINT_BASE + "/page", params, status().isOk(), 4, 1).andDo(result_ -> {
			RestResponsePage<UsuarioListDTO> result = this.getPage(result_);
			testSePossuITodos(result);
		});

	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindByWithLogin() throws Exception {
		prepara();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("login", "login");

		util.testGetPage(ENDPOINT_BASE + "/page", params, status().isOk(), 3, 1).andDo(result_ -> {
			RestResponsePage<UsuarioListDTO> list = this.getPage(result_);
			assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf4.getPessoa().getNome())));
			assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf2.getPessoa().getNome())));
			assertTrue(list.stream().anyMatch(u -> u.getNome().equals(bd.funf3.getPessoa().getNome())));
		});

	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "GET" })
	public void testFindByWithEmail() throws Exception {
		prepara();
		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();

		String emailNPrincipal = bd.funf4.getPessoa().getEmail().stream().filter(e -> !e.isPrincipal()).findFirst()
				.get().getEmail();

		params.add("email", "example");

		util.testGetPage(ENDPOINT_BASE + "/page", params, status().isOk(), 4, 1).andDo(result_ -> {
			RestResponsePage<UsuarioListDTO> list = this.getPage(result_);
			testSePossuITodos(list);

		});

		params.clear();
		params.add("email", bd.funf4.getPessoa().getEmailPrincipal().getEmail());
		util.testGetPage(ENDPOINT_BASE + "/page", params, status().isOk(), 1, 1).andDo(result_ -> {
			RestResponsePage<UsuarioListDTO> list = this.getPage(result_);
			assertTrue(list.stream().allMatch(u -> u.getNome().equals(bd.funf4.getPessoa().getNome())));

		});

		params.clear();
		params.add("email", emailNPrincipal);
		util.testGetPage(ENDPOINT_BASE + "/page", params, status().isOk(), 0, 0).andDo(result_ -> {

		});

	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "DELETE" })
	public void testDeleteSucesso() throws Exception {
		this.bd.user5 = this.bd.getUsuarioService().save(UsuarioNewDTO.from(this.bd.user5));

		util.testDelete(ENDPOINT_BASE + "/" + bd.user5.getId(), status().isNoContent());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "DELETE" })
	public void testDeleteNotFound() throws Exception {

		util.testDelete(ENDPOINT_BASE + "/1", status().isNotFound());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "DELETe" })
	public void testDeleteSemPermissao() throws Exception {

		util.testDelete(ENDPOINT_BASE + "/1", status().isForbidden());
	}

	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "DELETe" })
	public void testDeleteDataIntegrition() throws Exception {

		util.testDelete(ENDPOINT_BASE + "/null", status().isBadRequest());
	}

}
