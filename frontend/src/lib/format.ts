const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const numero = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 3 })

export const formatarMoeda = (valor: number) => moeda.format(valor)

export const formatarNumero = (valor: number) => numero.format(valor)

export const lerNumero = (texto: string) => Number(texto.trim().replace(',', '.'))
