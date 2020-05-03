package com.alissondourado.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.alissondourado.cursomc.domain.Cidade;
import com.alissondourado.cursomc.domain.Cliente;
import com.alissondourado.cursomc.domain.Endereco;
import com.alissondourado.cursomc.domain.enums.Perfil;
import com.alissondourado.cursomc.domain.enums.TipoCliente;
import com.alissondourado.cursomc.dto.ClienteDTO;
import com.alissondourado.cursomc.dto.ClienteNewDTO;
import com.alissondourado.cursomc.repositories.ClienteRepository;
import com.alissondourado.cursomc.repositories.EnderecoRepository;
import com.alissondourado.cursomc.security.UserSS;
import com.alissondourado.cursomc.services.exeptions.AuthorizationException;
import com.alissondourado.cursomc.services.exeptions.DataIntegrityException;
import com.alissondourado.cursomc.services.exeptions.ObjectNotFoundException;

@Service
public class ClienteService {
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@Autowired
	private ClienteRepository repo;
	
	@Autowired
	private EnderecoRepository enderecoRepository;

	public Cliente find(Integer id) {
		
		UserSS user = UserService.authenticated();
		if(user == null || !user.hasRole(Perfil.ADMIN) && !id.equals(user.getId())) {
			throw new AuthorizationException("Acesso negado");
		}
		
		Optional<Cliente> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Cliente.class.getName()));
	}

	public Cliente insert(Cliente cliente) {
		cliente.setId(null);
		cliente = repo.save(cliente);
		enderecoRepository.saveAll(cliente.getEnderecos());
		return cliente;
	}

	public Cliente update(Cliente cliente) {
		Cliente newCliente = find(cliente.getId());
		updateData(newCliente, cliente);
		return repo.save(newCliente);
	}

	public void delete(Integer id) {
		find(id);
		try {

			repo.deleteById(id);

		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir porque há pedidos relacionados");
		}
	}

	public List<Cliente> findAll() {
		return repo.findAll();
	}

	public Page<Cliente> findPage(Integer page, Integer linesPerPage, String orderBy, String direction) {
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return repo.findAll(pageRequest);
	}

	public Cliente fromDTO(ClienteDTO clienteDTO) {
		return new Cliente(clienteDTO.getId(), clienteDTO.getNome(), clienteDTO.getEmail(), null, null, null);
	}

	public Cliente fromDTO(ClienteNewDTO clienteDTO) {

		Cliente cliente = new Cliente(null, clienteDTO.getNome(), clienteDTO.getEmail(), clienteDTO.getCpfOuCnpj(),
				TipoCliente.toEnum(clienteDTO.getTipo()), bCryptPasswordEncoder.encode(clienteDTO.getSenha()));

		Cidade cidade = new Cidade(clienteDTO.getCidadeId(), null, null);

		Endereco endereco = new Endereco(null, clienteDTO.getLogradouro(), clienteDTO.getNumero(),
				clienteDTO.getComplemento(), clienteDTO.getBairro(), clienteDTO.getCep(), cliente, cidade);

		cliente.getEnderecos().add(endereco);
		cliente.getTelefones().add(clienteDTO.getTelefone1());

		if (clienteDTO.getTelefone2() != null) {
			cliente.getTelefones().add(clienteDTO.getTelefone2());
		}

		if (clienteDTO.getTelefone3() != null) {
			cliente.getTelefones().add(clienteDTO.getTelefone3());
		}

		return cliente;
	}

	private void updateData(Cliente newCliente, Cliente cliente) {
		newCliente.setNome(cliente.getNome());
		newCliente.setEmail(cliente.getEmail());
	}
}
