# Integração Unified + redesign 0.4

Branch: `integration/unified-redesign-0.4-20261022`. Base: `4e374d1d1b16fcd16f0368e56ffe41e8369f7059` (`origin/main`, 22/10/2026). Nenhum merge na `main` faz parte desta integração.

## Auditoria anterior às alterações

O estado foi consultado diretamente no GitHub e confrontado com os diffs dos commits. Os checks abaixo são os dos PRs originais; não substituem a validação desta branch.

| PR | Estado observado | Revisão auditada | Checks anteriores | Tratamento |
| --- | --- | --- | --- | --- |
| [#3](https://github.com/AttackAlien/Alien-Client/pull/3) PanelStudio | MERGED | `1cbdd3cb0f507309f4d91a03638aa2085844d10b` (merge `aff4317`) | 2/2 aprovados | Preservar classes MIT, licença e animação temporal; adaptar ao tema 0.4. |
| [#4](https://github.com/AttackAlien/Alien-Client/pull/4) Sky Ultimate | MERGED | `acfe57a04bddd362694a6e14a8e88434e714b53a` (merge `a728350`) | 2/2 aprovados | Preservar cubemap 3×2, panorama, yaw, drift, brilho, estrelas e testes de framebuffer. |
| [#5](https://github.com/AttackAlien/Alien-Client/pull/5) Unified | MERGED | `a281bffbf482f4fd827530d8532a4394cb8f6722` (merge `3075e03`) | 5/5 aprovados | Manter Unified padrão, configuração, Combat Studio, presets e dois módulos reais; combate OFF inicialmente. |
| [#6](https://github.com/AttackAlien/Alien-Client/pull/6) Pacote instalável | OPEN | `a726a6fcbbcaba182c8d524299f061a981b98690` | 6/6 aprovados | Incorporar empacotador, testes, guia e workflow; adaptar README e vincular evidência ao artefato atual. |
| [#7](https://github.com/AttackAlien/Alien-Client/pull/7) Studios V2 | OPEN / draft | `1c2c55ad70b0966afc03c3339d3f2fe9fdd38db8` | 5/5 aprovados | Incorporar galeria/favoritos, quatro looks e três slots com cópias independentes; adaptar telas 0.4. |
| [#8](https://github.com/AttackAlien/Alien-Client/pull/8) Redesign | OPEN / draft / CONFLICTING | `479967c8818ea09ff3dce0c2006f7a262b81fcea` | Sem CI do PR; relatórios locais anteriores Legit/Full | Integrar o código, sem promover binários/capturas antigos a evidência Unified. |

A versão desta integração é **0.4.0-alpha.2**, distinta dos JARs de prévia 0.4.0-alpha.1 do #8.

## Resolução e preservação

O merge local do #8 revelou conflitos em menus, componentes, World Studio, renderizador/configuração de céu, smoke e documentos. O código Unified existente é a base. As telas do redesign são adaptadas aos recursos já incorporados; não se substitui a base por um snapshot antigo do #8. Os recursos dos #6/#7 são portados seletivamente, sem alterar seus PRs originais.

- Os nove switches visuais continuam disponíveis nas três composições; Unified conserva também aim assist/click assist e os presets OFF/LEGIT/FULL/CUSTOM.
- A configuração `config/alien-client/` da Unified e os diretórios históricos continuam compatíveis. Temas e perfis antigos recebem os novos padrões por normalização.
- Cubemap e panorama continuam renderizações distintas. Galeria/favoritos, controles cinematográficos, clima local, partículas e restauração vanilla coexistem com o novo layout.
- Cosméticos mantêm malhas, tecido, imports, UUIDs, transformações e prévia 3D; os looks e slots V2 somam-se aos controles de iluminação/fundo do redesign.
- Fontes Open Sans, blur opcional, HSV/RGBA/HEX, escala de interface, HUD import/export, favoritos e atalhos individuais permanecem no sistema compartilhado.
- A migração de perfis anteriores reaproveita cor, opacidade e velocidade legadas quando o JSON ainda não contém `ui`; tokens 0.4 explícitos prevalecem. A velocidade não multiplica duas vezes seus aliases sincronizados.
- Documentos históricos e binários anteriores ficam identificados por versão. A galeria integrada deve usar capturas novas do executável desta branch.

## Validação desta integração

Em execução. Resultados anteriores não contam como aprovação desta revisão. Comandos, contagens, hashes, capturas reais e limitações serão registrados aqui após os testes.
