import type { NovoProduto } from './types'

export const CANETA: NovoProduto = {
  nome: 'Caneta',
  itens: [
    { nome: 'Caneta', pai: null, quantidade: 1, valor: 2.5 },
    { nome: 'Tinta', pai: 'Caneta', quantidade: 1, valor: 0.8 },
    { nome: 'Pigmento', pai: 'Tinta', quantidade: 1, valor: 0.3 },
    { nome: 'Solvente', pai: 'Tinta', quantidade: 1, valor: 0.5 },
    { nome: 'Corpo', pai: 'Caneta', quantidade: 1, valor: 1.0 },
    { nome: 'Plástico', pai: 'Corpo', quantidade: 1, valor: 0.7 },
    { nome: 'Clip', pai: 'Corpo', quantidade: 1, valor: 0.3 },
    { nome: 'Tampa', pai: 'Caneta', quantidade: 1, valor: 0.7 },
    { nome: 'Plástico', pai: 'Tampa', quantidade: 1, valor: 0.5 },
    { nome: 'Vedação', pai: 'Tampa', quantidade: 1, valor: 0.2 },
  ],
}

export const COMPUTADOR: NovoProduto = {
  nome: 'Computador',
  itens: [
    { nome: 'Computador', pai: null, quantidade: 1, valor: 2500 },
    { nome: 'Gabinete', pai: 'Computador', quantidade: 1, valor: 800 },
    { nome: 'Placa-Mãe', pai: 'Gabinete', quantidade: 1, valor: 500 },
    { nome: 'CPU', pai: 'Placa-Mãe', quantidade: 1, valor: 1000 },
    { nome: 'Memória RAM', pai: 'Placa-Mãe', quantidade: 2, valor: 200 },
    { nome: 'Chipset', pai: 'Placa-Mãe', quantidade: 1, valor: 150 },
    { nome: 'Fonte de Alimentação', pai: 'Gabinete', quantidade: 1, valor: 300 },
    { nome: 'Armazenamento', pai: 'Gabinete', quantidade: 1, valor: 0 },
    { nome: 'HDD', pai: 'Armazenamento', quantidade: 1, valor: 250 },
    { nome: 'SSD', pai: 'Armazenamento', quantidade: 1, valor: 400 },
    { nome: 'Monitor', pai: 'Computador', quantidade: 1, valor: 900 },
    { nome: 'Tela LCD', pai: 'Monitor', quantidade: 1, valor: 600 },
    { nome: 'Carcaça', pai: 'Monitor', quantidade: 1, valor: 100 },
    { nome: 'Periféricos', pai: 'Computador', quantidade: 1, valor: 0 },
    { nome: 'Teclado', pai: 'Periféricos', quantidade: 1, valor: 100 },
    { nome: 'Mouse', pai: 'Periféricos', quantidade: 1, valor: 80 },
  ],
}

export const EXEMPLOS = [CANETA, COMPUTADOR]
