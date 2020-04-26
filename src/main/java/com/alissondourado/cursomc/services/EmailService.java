package com.alissondourado.cursomc.services;

import org.springframework.mail.SimpleMailMessage;

import com.alissondourado.cursomc.domain.Pedido;

public interface EmailService {

	void sendOrderConfirmationEmail(Pedido pedido);

	void sendEmail(SimpleMailMessage msg);
}
