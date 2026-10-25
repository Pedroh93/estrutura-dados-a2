import { useState, type FormEvent } from 'react'
import { lerNumero } from '../../../lib/format'

type Props = {
  valorAtual: number
  onSalvar: (valor: number) => Promise<void>
}

export function EditorValor({ valorAtual, onSalvar }: Props) {
  const [texto, setTexto] = useState(String(valorAtual))
  const [salvando, setSalvando] = useState(false)

  const valor = lerNumero(texto)
  const valido = texto.trim() !== '' && Number.isFinite(valor) && valor >= 0
  const alterado = valido && Math.abs(valor - valorAtual) >= 0.005

  async function salvar(evento: FormEvent) {
    evento.preventDefault()
    if (!alterado) return
    setSalvando(true)
    try {
      await onSalvar(valor)
    } finally {
      setSalvando(false)
    }
  }

  return (
    <form className="editor-valor" onSubmit={salvar}>
      <input
        type="number"
        min={0}
        step="0.01"
        aria-label="Novo valor"
        value={texto}
        aria-invalid={!valido}
        onChange={(e) => setTexto(e.target.value)}
      />
      <button type="submit" className="botao botao-pequeno" disabled={!alterado || salvando}>
        {salvando ? '...' : 'Salvar'}
      </button>
    </form>
  )
}
