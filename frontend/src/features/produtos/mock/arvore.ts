import type { AlteracaoValor, Necessidade, NoArvore } from '../types'

export type ItemArmazenado = {
  id: number
  nome: string
  paiId: number | null
  quantidade: number
  valor: number
}

export class TreeNode<T> {
  data: T
  parent: TreeNode<T> | null = null
  children: TreeNode<T>[] = []

  constructor(data: T) {
    this.data = data
  }

  addChild(child: TreeNode<T>) {
    child.parent = this
    this.children.push(child)
    return child
  }

  isLeaf() {
    return this.children.length === 0
  }

  *preOrder(): Generator<TreeNode<T>> {
    yield this
    for (const child of this.children) yield* child.preOrder()
  }

  *postOrder(): Generator<TreeNode<T>> {
    for (const child of this.children) yield* child.postOrder()
    yield this
  }
}

const arredondar = (valor: number) => Math.round(valor * 100) / 100

export function buildTree(itens: ItemArmazenado[]): TreeNode<ItemArmazenado> {
  const nos = new Map(itens.map((item) => [item.id, new TreeNode(item)]))
  let raiz: TreeNode<ItemArmazenado> | null = null

  for (const item of itens) {
    const no = nos.get(item.id)!

    if (item.paiId === null) {
      if (raiz) throw new Error('A fórmula deve ter apenas um item raiz')
      raiz = no
      continue
    }

    const pai = nos.get(item.paiId)
    if (!pai) throw new Error(`O pai do item "${item.nome}" não existe`)
    pai.addChild(no)
  }

  if (!raiz) throw new Error('A fórmula não possui item raiz')
  if ([...raiz.preOrder()].length !== itens.length) throw new Error('A fórmula contém um ciclo')

  return raiz
}

export function explodir(no: TreeNode<ItemArmazenado>): NoArvore {
  const { id, nome, quantidade, valor } = no.data
  return {
    id,
    nome,
    quantidade,
    valorUnitario: valor,
    valorTotal: arredondar(valor * quantidade),
    filhos: no.children.map(explodir),
  }
}

export function calcularNecessidade(raiz: TreeNode<ItemArmazenado>, quantidade: number): Necessidade {
  const porNome = new Map<string, { quantidade: number; valorTotal: number }>()

  const visitar = (no: TreeNode<ItemArmazenado>, multiplicador: number) => {
    if (no.isLeaf()) {
      const acumulado = porNome.get(no.data.nome) ?? { quantidade: 0, valorTotal: 0 }
      acumulado.quantidade += multiplicador
      acumulado.valorTotal += multiplicador * no.data.valor
      porNome.set(no.data.nome, acumulado)
      return
    }
    for (const filho of no.children) visitar(filho, multiplicador * filho.data.quantidade)
  }

  visitar(raiz, quantidade)

  const componentes = [...porNome].map(([nome, c]) => ({
    nome,
    quantidade: c.quantidade,
    valorUnitario: arredondar(c.valorTotal / c.quantidade),
    valorTotal: arredondar(c.valorTotal),
  }))

  return {
    quantidade,
    componentes,
    valorTotal: arredondar([...porNome.values()].reduce((soma, c) => soma + c.valorTotal, 0)),
  }
}

export function implodir(raiz: TreeNode<ItemArmazenado>): AlteracaoValor[] {
  const alteracoes: AlteracaoValor[] = []

  for (const no of raiz.postOrder()) {
    if (no.isLeaf()) continue

    const novoValor = arredondar(
      no.children.reduce((soma, filho) => soma + filho.data.valor * filho.data.quantidade, 0),
    )

    if (Math.abs(novoValor - no.data.valor) >= 0.005) {
      alteracoes.push({ id: no.data.id, nome: no.data.nome, valorAntigo: no.data.valor, valorNovo: novoValor })
      no.data.valor = novoValor
    }
  }

  return alteracoes
}
