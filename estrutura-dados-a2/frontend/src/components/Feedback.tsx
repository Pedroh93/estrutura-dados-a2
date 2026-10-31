export function Carregando({ texto = 'Carregando...' }: { texto?: string }) {
  return <p className="feedback">{texto}</p>
}

export function MensagemErro({ mensagem }: { mensagem: string }) {
  return (
    <p className="alerta alerta-erro" role="alert">
      {mensagem}
    </p>
  )
}
