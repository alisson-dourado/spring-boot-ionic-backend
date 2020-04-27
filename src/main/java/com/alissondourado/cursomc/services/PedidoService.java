package com.alissondourado.cursomc.services;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alissondourado.cursomc.domain.ItemPedido;
import com.alissondourado.cursomc.domain.PagamentoComBoleto;
import com.alissondourado.cursomc.domain.Pedido;
import com.alissondourado.cursomc.domain.enums.EstadoPagamento;
import com.alissondourado.cursomc.repositories.ItemPedidoRepository;
import com.alissondourado.cursomc.repositories.PagamentoRepository;
import com.alissondourado.cursomc.repositories.PedidoRepository;
import com.alissondourado.cursomc.services.exeptions.ObjectNotFoundException;

@Service
public class PedidoService {

	@Autowired
	private PedidoRepository repo;
	
	@Autowired
	private PagamentoRepository pagamentoRepository; 
	
	@Autowired
	private ItemPedidoRepository itemPedidoRepository;
	
	@Autowired
	private BoletoService boletoService;
	
	@Autowired
	private ProdutoService produtoService;
	
	@Autowired
	private ClienteService clienteService;
	
	@Autowired
	private EmailService emailService;

	public Pedido find(Integer id) {
		Optional<Pedido> obj = repo.findById(id);
		return obj.orElseThrow(() -> new ObjectNotFoundException(
				"Objeto não encontrado! Id: " + id + ", Tipo: " + Pedido.class.getName()));
	}
	
	@Transactional
	public Pedido insert(Pedido pedido) {
		pedido.setId(null);
		pedido.setInstante(new Date());
		pedido.setCliente(clienteService.find(pedido.getCliente().getId()));
		pedido.getPagamento().setEstado(EstadoPagamento.PENDENTE);
		pedido.getPagamento().setPedido(pedido);
		
		if(pedido.getPagamento() instanceof PagamentoComBoleto) {
			PagamentoComBoleto pagto = (PagamentoComBoleto) pedido.getPagamento();
			boletoService.preencherPagamentoComBoleto(pagto, pedido.getInstante());
		}
		pedido = repo.save(pedido);
		pagamentoRepository.save(pedido.getPagamento());
		for(ItemPedido item : pedido.getItens()) {
			item.setDesconto(0.0);
			item.setProduto(produtoService.find(item.getProduto().getId()));
			item.setPreco(item.getProduto().getPreco());
			item.setPedido(pedido);
		}
		itemPedidoRepository.saveAll(pedido.getItens());
		emailService.sendOrderConfirmationHtmlEmail(pedido);
		return pedido;
	}
}
