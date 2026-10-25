import { Link, NavLink, Route, Routes } from 'react-router'
import { USANDO_MOCK } from './features/produtos/api'
import { CadastroProdutoPage } from './features/produtos/pages/CadastroProdutoPage'
import { ListaProdutosPage } from './features/produtos/pages/ListaProdutosPage'
import { ProdutoDetalhePage } from './features/produtos/pages/ProdutoDetalhePage'

function App() {
  return (
    <>
      <header className="topo">
        <div className="conteudo topo-conteudo">
          <Link to="/" className="marca">
            Fórmulas de Produto
          </Link>
          <nav className="menu">
            <NavLink to="/" end>
              Produtos
            </NavLink>
            <NavLink to="/produtos/novo">Novo produto</NavLink>
          </nav>
          {USANDO_MOCK && <span className="etiqueta etiqueta-aviso">Dados mockados</span>}
        </div>
      </header>

      <main className="conteudo">
        <Routes>
          <Route path="/" element={<ListaProdutosPage />} />
          <Route path="/produtos/novo" element={<CadastroProdutoPage />} />
          <Route path="/produtos/:id" element={<ProdutoDetalhePage />} />
          <Route path="*" element={<p className="feedback">Página não encontrada.</p>} />
        </Routes>
      </main>
    </>
  )
}

export default App
