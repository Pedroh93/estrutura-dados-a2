import { Link } from 'react-router'
import { Carregando, MensagemErro } from '../../../components/Feedback'
import { formatarMoeda } from '../../../lib/format'
import { useRequest } from '../../../lib/useRequest'
import { USANDO_MOCK, produtosApi, resetarDadosMock } from '../api'

export function ListaProdutosPage() {
  const { data: produtos, error, loading, reload } = useRequest(() => produtosApi.listarProdutos(), 'produtos')

  function restaurarExemplos() {
    if (!confirm('Apagar os dados mockados e restaurar Caneta e Computador?')) return
    resetarDadosMock()
    reload()
  }

  return (
    <>
      <div className="cabecalho-pagina">
        <h1>Produtos</h1>
        <div className="linha-form">
          {USANDO_MOCK && (
            <button type="button" className="botao botao-secundario" onClick={restaurarExemplos}>
              Restaurar exemplos
            </button>
          )}
          <Link to="/produtos/novo" className="botao">
            Novo produto
          </Link>
        </div>
      </div>

      {loading && !produtos && <Carregando />}
      {error && <MensagemErro mensagem={error} />}

      {produtos && produtos.length === 0 && (
        <p className="feedback">
          Nenhum produto cadastrado. <Link to="/produtos/novo">Cadastre o primeiro</Link>.
        </p>
      )}

      {produtos && produtos.length > 0 && (
        <table className="tabela">
          <thead>
            <tr>
              <th>Produto</th>
              <th className="num">Itens na fórmula</th>
              <th className="num">Valor cadastrado</th>
              <th />
            </tr>
          </thead>
          <tbody>
            {produtos.map((produto) => (
              <tr key={produto.id}>
                <td>{produto.nome}</td>
                <td className="num">{produto.quantidadeItens}</td>
                <td className="num">{formatarMoeda(produto.valorTotal)}</td>
                <td className="acao">
                  <Link to={`/produtos/${produto.id}`}>Ver detalhes</Link>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </>
  )
}
