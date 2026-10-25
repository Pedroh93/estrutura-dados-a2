import { formatarMoeda } from '../../../lib/format'
import { custoDesatualizado, custoPelasFolhas } from '../custos'
import type { NoArvore } from '../types'
import { ArvoreProduto } from './ArvoreProduto'

export function AbaExplosao({ arvore }: { arvore: NoArvore }) {
  return (
    <section>
      <p className="descricao">
        Estrutura completa do produto. Quantidades são por unidade do item pai.
      </p>

      {custoDesatualizado(arvore) && (
        <p className="alerta alerta-aviso">
          Alguns valores intermediários não batem com a soma dos componentes. Custo pelos componentes
          básicos: <strong>{formatarMoeda(custoPelasFolhas(arvore))}</strong>. Use a aba "Preços e
          implosão" para recalcular.
        </p>
      )}

      <ArvoreProduto raiz={arvore} />
    </section>
  )
}
