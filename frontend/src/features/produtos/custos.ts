import type { NoArvore } from './types'

export function custoPelasFolhas(no: NoArvore): number {
  if (no.filhos.length === 0) return no.valorUnitario
  return no.filhos.reduce((soma, filho) => soma + custoPelasFolhas(filho) * filho.quantidade, 0)
}

export function custoDesatualizado(no: NoArvore): boolean {
  if (no.filhos.length === 0) return false
  const somaFilhos = no.filhos.reduce((soma, filho) => soma + filho.valorUnitario * filho.quantidade, 0)
  return Math.abs(somaFilhos - no.valorUnitario) >= 0.005 || no.filhos.some(custoDesatualizado)
}
