package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.ConnectionFactory;
import br.com.alura.bytebank.domain.RegraDeNegocioException;

import java.math.BigDecimal;
import java.sql.Connection;
import java.util.Set;

public class ContaService {
    private ConnectionFactory connection;

    public ContaService() {
        this.connection = new ConnectionFactory();
    }

    public Set<Conta> listarContasAbertas() {
        Connection dbConnection = this.connection.retrieveConnection();

        return new ContaDAO(dbConnection).listar();
    }

    public BigDecimal consultarSaldo(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);
        return conta.getSaldo();
    }

    public void abrir(DadosAberturaConta dadosDaConta) {
        Connection dbConnection = this.connection.retrieveConnection();

        new ContaDAO(dbConnection).salvar(dadosDaConta);
    }

    public void realizarSaque(Integer numeroDaConta, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do saque deve ser superior a zero!");
        }

        var conta = buscarContaPorNumero(numeroDaConta);

        if (valor.compareTo(conta.getSaldo()) > 0) {
            throw new RegraDeNegocioException("Saldo insuficiente!");
        }

        var saldoEmConta = conta.getSaldo();

        var novoSaldo = saldoEmConta.subtract(valor);

        Connection dbConnection = this.connection.retrieveConnection();

        new ContaDAO(dbConnection).alterarSaldo(conta.getNumero(), novoSaldo);
    }

    public void realizarDeposito(Integer numeroDaConta, BigDecimal valor) {
        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RegraDeNegocioException("Valor do deposito deve ser superior a zero!");
        }

        var conta = buscarContaPorNumero(numeroDaConta);

        var saldoEmConta = conta.getSaldo();

        var novoSaldo = saldoEmConta.add(valor);

        Connection dbConnection = this.connection.retrieveConnection();

        new ContaDAO(dbConnection).alterarSaldo(conta.getNumero(), novoSaldo);
    }

    public void encerrar(Integer numeroDaConta) {
        var conta = buscarContaPorNumero(numeroDaConta);

        if (conta.possuiSaldo()) {
            throw new RegraDeNegocioException("Conta não pode ser encerrada pois ainda possui saldo!");
        }

        Connection dbConnection = this.connection.retrieveConnection();

        new ContaDAO(dbConnection).encerrar(numeroDaConta);
    }

    public Conta buscarContaPorNumero(Integer numero) {
        Connection dbConnection = this.connection.retrieveConnection();

        Conta conta = new ContaDAO(dbConnection).buscarContaComNumero(numero);

        if(conta != null) {
            return conta;
        } else {
            throw new RegraDeNegocioException("Não existe conta cadastrada com esse número!");
        }
    }

    public boolean realizarTransferencia(Conta contaOrigem, Conta contaDestino, BigDecimal valor) {
        try {
            this.realizarSaque(contaOrigem.getNumero(), valor);
            this.realizarDeposito(contaDestino.getNumero(), valor);
            return true;
        } catch (RegraDeNegocioException e) {
            System.out.println("Falha na transferência: " + e.getMessage());
            return false;
        }
    }
}
