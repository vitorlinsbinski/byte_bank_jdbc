package br.com.alura.bytebank.domain.conta;

import br.com.alura.bytebank.domain.cliente.Cliente;
import br.com.alura.bytebank.domain.cliente.DadosCadastroCliente;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class ContaDAO {
    private Connection connection;

    ContaDAO(Connection connection) {
        this.connection = connection;
    }

    public void salvar(DadosAberturaConta dadosDaConta) {
        var cliente = new Cliente(dadosDaConta.dadosCliente());
        var conta = new Conta(dadosDaConta.numero(), cliente);

        String sql = "INSERT INTO conta (numero, saldo, cliente_nome, cliente_cpf, cliente_email)" +
                "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);

            preparedStatement.setInt(1, conta.getNumero());
            preparedStatement.setBigDecimal(2, BigDecimal.ZERO);
            preparedStatement.setString(3, dadosDaConta.dadosCliente().nome());
            preparedStatement.setString(4, dadosDaConta.dadosCliente().cpf());
            preparedStatement.setString(5, dadosDaConta.dadosCliente().email());

            preparedStatement.execute();

            preparedStatement.close();
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Set<Conta> listar() {
        Set<Conta> contas = new HashSet<>();

        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        String sql = "SELECT * FROM conta WHERE esta_ativa = true";

        try {
            preparedStatement = this.connection.prepareStatement(sql);
            resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                Integer numero = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);
                boolean estaAtiva = resultSet.getBoolean(6);

                DadosCadastroCliente dadosCadastroCliente = new DadosCadastroCliente(nome, cpf, email);

                Cliente cliente = new Cliente(dadosCadastroCliente);

                contas.add(new Conta(numero, cliente, saldo, estaAtiva));
            }

            return contas;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if(preparedStatement != null) {
                    preparedStatement.close();
                }

                if(resultSet != null) {
                    resultSet.close();
                }

                if(this.connection != null && !this.connection.isClosed()) {
                    this.connection.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Conta buscarContaComNumero(Integer numeroDaConta) {
        String sql = "SELECT * FROM conta WHERE numero = ? AND esta_ativa = true";
        Conta conta = null;

        try  {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, numeroDaConta);

            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()) {
                Integer numero = resultSet.getInt(1);
                BigDecimal saldo = resultSet.getBigDecimal(2);
                String nome = resultSet.getString(3);
                String cpf = resultSet.getString(4);
                String email = resultSet.getString(5);
                boolean estaAtiva = resultSet.getBoolean(6);

                DadosCadastroCliente dadosCadastroCliente = new DadosCadastroCliente(nome, cpf, email);

                Cliente cliente = new Cliente(dadosCadastroCliente);

                conta = new Conta(numero, cliente, saldo, estaAtiva);
            }

            resultSet.close();
            preparedStatement.close();
            this.connection.close();

            return conta;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void alterarSaldo(Integer numeroDaConta, BigDecimal valor) {
        String sqlUpdateSaldo = "UPDATE conta SET saldo = ? WHERE numero = ?";

        try {
            this.connection.setAutoCommit(false);

            PreparedStatement updatePreparedStatement = this.connection.prepareStatement(sqlUpdateSaldo);

            updatePreparedStatement.setBigDecimal(1, valor);
            updatePreparedStatement.setInt(2, numeroDaConta);

            updatePreparedStatement.executeUpdate();

            this.connection.commit();

            updatePreparedStatement.close();
            this.connection.close();

        } catch (SQLException e) {
            try {
                this.connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }

            throw new RuntimeException(e);
        }
    }

    public void encerrar(Integer numeroDaConta) {
        String sql = "UPDATE conta SET esta_ativa = 0 WHERE numero = ?";

        try {
            PreparedStatement preparedStatement = this.connection.prepareStatement(sql);
            preparedStatement.setInt(1, numeroDaConta);
            preparedStatement.executeUpdate();

            preparedStatement.close();
            this.connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
