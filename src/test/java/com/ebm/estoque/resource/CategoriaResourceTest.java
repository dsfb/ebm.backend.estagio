package com.ebm.estoque.resource;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.ebm.TestUtils;
import com.ebm.estoque.domain.CategoriaItem;
import com.ebm.estoque.service.interfaces.CategoriaItemService;
import com.ebm.geral.service.PopulaBD;

@ActiveProfiles("testauto")
@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class CategoriaResourceTest {

	@Autowired
	private CategoriaItemService catServ;
	@Autowired
	private PopulaBD bd;

	@Autowired
	private TestUtils util;

	private final String ENDPOINT_BASE = "/categorias";
	private final String BASE_AUTHORITY = "ITEM_AUX_";

	@Before
	public void setUp() {
		bd.instanciaCategorias();

	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testaAdicionarCategoriaDeveAceitar() throws Exception {
		util.testPostExpectCreated(ENDPOINT_BASE, bd.cat1);
	}
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY  })
	public void testaAdicionarCategoriaDeveRejeitarCredencial() throws Exception {
		util.testPostExpectForbidden(ENDPOINT_BASE, bd.cat1);
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testaAdicionarCategoriaObjetoInvalido() throws Exception {
		bd.cat1.setNome(null);
		util.testPost(ENDPOINT_BASE, bd.cat1, status().isUnprocessableEntity());
	}
	
	// aqui deve ficar o teste para ver a validação do nome repetido.
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "PUT" })
	public void testeUpdateCategoria() throws Exception {
		bd.cat1 = catServ.save(bd.cat1);
		util.em().detach(bd.cat1);
		bd.cat1.setNome("novonome");
		CategoriaItem find = catServ.findById(bd.cat1.getId());
		
		assertFalse(bd.cat1.getNome().equals(find.getNome()));
		
		util.testPutExpectSucess(ENDPOINT_BASE+"/"+bd.cat1.getId(), bd.cat1)	;
		find = catServ.findById(bd.cat1.getId());
		assertTrue(bd.cat1.getNome().equals(find.getNome()));
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY  })
	public void testaUpdateCategoriaDeveRejeitarCredencial() throws Exception {
		util.testPutExpectedForbidden(ENDPOINT_BASE+"/1", bd.cat1);
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "POST" })
	public void testaUpdateCategoriaObjetoInvalido() throws Exception {
		bd.cat1 = catServ.save(bd.cat1);
		util.em().detach(bd.cat1);
		bd.cat1.setNome("novonome");
		CategoriaItem find = catServ.findById(bd.cat1.getId());
		assertFalse(bd.cat1.getNome().equals(find.getNome()));
		bd.cat1.setNome(null);
		util.testPut(ENDPOINT_BASE+"/"+bd.cat1.getId(), bd.cat1, status().isUnprocessableEntity());
	}
	
	
	// aqui deve ficar o teste para ver a validação do nome repetido.
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "DELETE", BASE_AUTHORITY + "GET" })
	public void testDeleteCategoria() throws Exception {
		bd.cat1 = catServ.save(bd.cat1);
		util.em().detach(bd.cat1);
		util.testDelete(ENDPOINT_BASE+"/"+bd.cat1.getId(), status().isNoContent())	;
		util.testGet(ENDPOINT_BASE, bd.cat1.getId(), status().isNotFound());
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY })
	public void testDeleteCategoriaFalhaCredencial() throws Exception {
		util.testDelete(ENDPOINT_BASE+"/1", status().isForbidden());
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = { BASE_AUTHORITY + "DELETE" })
	public void testDeleteNotFound() throws Exception {	
		util.testDelete(ENDPOINT_BASE+"/1", status().isNotFound());
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = {BASE_AUTHORITY + "GET" })
	public void testFindById() throws Exception {
		bd.cat1 = catServ.save(bd.cat1);
		util.em().detach(bd.cat1);
		util.testGet(ENDPOINT_BASE, bd.cat1.getId(), status().isOk());
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = {BASE_AUTHORITY + "" })
	public void testFindByIdFalhaCredencial() throws Exception {
		bd.cat1 = catServ.save(bd.cat1);
		util.em().detach(bd.cat1);
		util.testGet(ENDPOINT_BASE, bd.cat1.getId(), status().isForbidden());
	}
	
	@Transactional
	@Test
	@WithMockUser(username = "test", password = "test", authorities = {BASE_AUTHORITY + "GET" })
	public void testFindByIdNotFound() throws Exception {
		util.testGet(ENDPOINT_BASE, 1, status().isNotFound());
		
	}
	
}
