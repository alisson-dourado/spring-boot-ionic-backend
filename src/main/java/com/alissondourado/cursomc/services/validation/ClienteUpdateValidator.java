package com.alissondourado.cursomc.services.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerMapping;

import com.alissondourado.cursomc.domain.Cliente;
import com.alissondourado.cursomc.dto.ClienteDTO;
import com.alissondourado.cursomc.repositories.ClienteRepository;
import com.alissondourado.cursomc.resources.exception.FieldMessage;

public class ClienteUpdateValidator implements ConstraintValidator<ClienteUpdate, ClienteDTO> {
	
	@Autowired
	HttpServletRequest request;
	
	@Autowired
	private ClienteRepository repo;

	@Override
	public void initialize(ClienteUpdate constraintAnnotation) {
	}

	@Override
	public boolean isValid(ClienteDTO clienteNewDTO, ConstraintValidatorContext context) {
		
		@SuppressWarnings("unchecked")
		Map<String, String> map = (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
		Integer uriClienteId = Integer.parseInt(map.get("id"));
		
		List<FieldMessage> errors = new ArrayList<>();
		
		Cliente cliente = repo.findByEmail(clienteNewDTO.getEmail());
		if(cliente != null && !cliente.getId().equals(uriClienteId)) {
			errors.add(new FieldMessage("email", "Email já existente"));
		}

		for (FieldMessage error : errors) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(error.getMessage()).addPropertyNode(error.getFieldName())
					.addConstraintViolation();
		}

		return errors.isEmpty();
	}

}
