package com.alissondourado.cursomc.services.validation;

import java.util.ArrayList;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.alissondourado.cursomc.domain.enums.TipoCliente;
import com.alissondourado.cursomc.dto.ClienteNewDTO;
import com.alissondourado.cursomc.resources.exception.FieldMessage;
import com.alissondourado.cursomc.services.validation.utils.BR;

public class ClienteInsertValidator implements ConstraintValidator<ClienteInsert, ClienteNewDTO> {

	@Override
	public void initialize(ClienteInsert constraintAnnotation) {
	}

	@Override
	public boolean isValid(ClienteNewDTO clienteNewDTO, ConstraintValidatorContext context) {

		List<FieldMessage> errors = new ArrayList<>();
		
		if(clienteNewDTO.getTipo().equals(TipoCliente.PESSOAFISICA.getCod()) && !BR.isValidCPF(clienteNewDTO.getCpfOuCnpj())){
			errors.add(new FieldMessage("cpfOuCnpj", "CPF inválido"));
		}
		
		if(clienteNewDTO.getTipo().equals(TipoCliente.PESSOAJURIDICA.getCod()) && !BR.isValidCNPJ(clienteNewDTO.getCpfOuCnpj())){
			errors.add(new FieldMessage("cpfOuCnpj", "CNPJ inválido"));
		}

		for (FieldMessage error : errors) {
			context.disableDefaultConstraintViolation();
			context.buildConstraintViolationWithTemplate(error.getMessage()).addPropertyNode(error.getFieldName())
					.addConstraintViolation();
		}

		return errors.isEmpty();
	}

}
