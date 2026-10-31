import { useState, type FormEvent } from 'react'
import { MensagemErro } from '../../../components/Feedback'
import { formatarMoeda, formatarNumero, lerNumero } from '../../../lib/format'
import { produtosApi } from '../api'
import type { Necessidade } from '../types'

export function AbaProducao({ produtoId, nomeProduto }: { produtoId: number; nomeProduto: string }) {
  const [quantidade, setQuantidade] = useState('1')
  const [resultado, setResultado] = useState<Necessidade | null>(null)
  const [erro, setErro] = useState<string | null>(null)
  const [calculando, setCalculando] = useState(false)

  async function calcular(evento: FormEvent) {
    evento.preventDefault()
    const valor = lerNumero(quantidade)
    if (!Number.isInteger(valor) || valor <= 0) {
      setErro('Informe uma quantidade inteira maior que zero')
      return
    }

    setErro(null)
    setCalculando(true)
    try {
      setResultado(await produtosApi.calcularNecessidade(produtoId, valor))
    } catch (err) {
      setErro((err as Error).message)
    } finally {
      setCalculando(false)
    }
  }

  return (
    <section>
      <p className="descricao">
        Informe quantas unidades de {nomeProduto} deseja produzir para ver os componentes básicos
        necessários e o valor de aquisição.
      </p>

      <form className="linha-form" onSubmit={calcular}>
        <label>
          Quantidade a produzir
          <input
            type="number"
            min={1}
            step={1}
            value={quantidade}
            onChange={(e) => setQuantidade(e.target.value)}
          />
        </label>
        <button type="submit" className="botao" disabled={calculando}>
          {calculando ? 'Calculando...' : 'Calcular'}
        </button>
      </form>

      {erro && <MensagemErro mensagem={erro} />}

      {resultado && (
        <>
          <table className="tabela">
            <thead>
              <tr>
                <th>Componente</th>
                <th className="num">Quantidade</th>
                <th className="num">Valor unit.</th>
                <th className="num">Valor total</th>
              </tr>
            </thead>
            <tbody>
              {resultado.componentes.map((c) => (
                <tr key={c.nome}>
                  <td>{c.nome}</td>
                  <td className="num">{formatarNumero(c.quantidade)}</td>
                  <td className="num">{formatarMoeda(c.valorUnitario)}</td>
                  <td className="num">{formatarMoeda(c.valorTotal)}</td>
                </tr>
              ))}
            </tbody>
            <tfoot>
              <tr>
                <td>Total para {formatarNumero(resultado.quantidade)} unidade(s)</td>
                <td className="num">
                  {formatarNumero(resultado.componentes.reduce((soma, c) => soma + c.quantidade, 0))}
                </td>
                <td />
                <td className="num">{formatarMoeda(resultado.valorTotal)}</td>
              </tr>
            </tfoot>
          </table>
          <p className="nota">
            Componentes com o mesmo nome em partes diferentes do produto são somados; o valor unitário
            mostrado é a média ponderada.
          </p>
        </>
      )}
    </section>
  )
}
