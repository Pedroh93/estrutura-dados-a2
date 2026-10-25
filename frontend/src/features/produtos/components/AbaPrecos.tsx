import { useState } from 'react'
import { MensagemErro } from '../../../components/Feedback'
import { formatarMoeda } from '../../../lib/format'
import { produtosApi } from '../api'
import { custoDesatualizado } from '../custos'
import type { AlteracaoValor, NoArvore } from '../types'
import { ArvoreProduto } from './ArvoreProduto'
import { EditorValor } from './EditorValor'

type Props = {
  produtoId: number
  arvore: NoArvore
  onArvoreAtualizada: (arvore: NoArvore) => void
}

export function AbaPrecos({ produtoId, arvore, onArvoreAtualizada }: Props) {
  const [alteracoes, setAlteracoes] = useState<AlteracaoValor[] | null>(null)
  const [destaques, setDestaques] = useState<Set<number>>(new Set())
  const [erro, setErro] = useState<string | null>(null)
  const [implodindo, setImplodindo] = useState(false)

  async function salvarValor(itemId: number, valor: number) {
    setErro(null)
    try {
      onArvoreAtualizada(await produtosApi.atualizarValorItem(produtoId, itemId, valor))
      setDestaques(new Set([itemId]))
      setAlteracoes(null)
    } catch (err) {
      setErro((err as Error).message)
    }
  }

  async function recalcular() {
    setErro(null)
    setImplodindo(true)
    try {
      const resultado = await produtosApi.implodir(produtoId)
      onArvoreAtualizada(resultado.arvore)
      setAlteracoes(resultado.alteracoes)
      setDestaques(new Set(resultado.alteracoes.map((a) => a.id)))
    } catch (err) {
      setErro((err as Error).message)
    } finally {
      setImplodindo(false)
    }
  }

  const desatualizado = custoDesatualizado(arvore)

  return (
    <section>
      <p className="descricao">
        Altere o preço dos componentes básicos e depois recalcule o custo do produto (implosão): cada
        item composto passa a valer a soma de valor × quantidade dos seus filhos.
      </p>

      <div className="linha-form">
        <button type="button" className="botao" onClick={recalcular} disabled={implodindo}>
          {implodindo ? 'Recalculando...' : 'Recalcular custo (implosão)'}
        </button>
        {desatualizado && <span className="etiqueta etiqueta-aviso">Custos desatualizados</span>}
      </div>

      {erro && <MensagemErro mensagem={erro} />}

      {alteracoes && (
        <div className="resultado-implosao">
          {alteracoes.length === 0 ? (
            <p className="alerta alerta-sucesso">Nenhum valor mudou: o custo já estava consistente.</p>
          ) : (
            <table className="tabela">
              <thead>
                <tr>
                  <th>Item recalculado</th>
                  <th className="num">Antes</th>
                  <th className="num">Depois</th>
                  <th className="num">Diferença</th>
                </tr>
              </thead>
              <tbody>
                {alteracoes.map((a) => (
                  <tr key={a.id}>
                    <td>{a.nome}</td>
                    <td className="num">{formatarMoeda(a.valorAntigo)}</td>
                    <td className="num">{formatarMoeda(a.valorNovo)}</td>
                    <td className={`num ${a.valorNovo > a.valorAntigo ? 'positivo' : 'negativo'}`}>
                      {a.valorNovo > a.valorAntigo ? '+' : ''}
                      {formatarMoeda(a.valorNovo - a.valorAntigo)}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      <ArvoreProduto
        raiz={arvore}
        destaques={destaques}
        colunaExtra={{
          titulo: 'Novo valor unit.',
          render: (no) =>
            no.filhos.length === 0 ? (
              <EditorValor
                key={`${no.id}-${no.valorUnitario}`}
                valorAtual={no.valorUnitario}
                onSalvar={(valor) => salvarValor(no.id, valor)}
              />
            ) : (
              <span className="nota">calculado pelos filhos</span>
            ),
        }}
      />
    </section>
  )
}
