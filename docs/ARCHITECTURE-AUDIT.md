# Auditoria arquitetural — base 0.2.0 e separação Legit/Full

Auditoria de leitura do repositório para a Fase 1 do novo master plan. A referência preservada é o commit `b07279bff0b17798d09a6a8be485baf3a1327264` da versão 0.2.0. Este documento distingue essa base já existente da arquitetura recomendada e das funcionalidades futuras; não anuncia a implementação de módulos Full.

## Evidência existente

- Minecraft Java 1.8.9, Forge `11.15.1.2318`, mappings `stable_22`, Java 8, LWJGL 2, ForgeGradle 2.1 e Gradle 2.14.1.
- 38 arquivos Java de produção, organizados em configuração, interface, HUD, cosméticos/malhas, mundo/efeitos e desempenho.
- **76 testes JUnit aprovados**, com zero falhas, erros ou testes ignorados, segundo os XMLs existentes em `build/test-results/`.
- **88 verificações gráficas aprovadas e 35 PNGs reais**, segundo `build/smoke/result.txt` e as capturas da rodada 0.2.0. A evidência preservada está na [galeria](../captures/README.md) e no [resultado original](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/captures/v0.2.0/result.txt).
- As verificações gráficas executaram classes atuais de desenvolvimento em Minecraft/Forge real, em mundo integrado descartável e OpenGL por software. Incluíram interface, controles, cosméticos, capa em movimento, céu a 2/4 chunks, panorama importado, perfis, preservação do horário/clima reais e encerramento pela ação vanilla.

Esses resultados pertencem à base 0.2.0. Não são testes de duas edições, benchmark comparativo, validação de launcher externo ou teste em servidor privado/público. O total de verificações inclui guardas e capturas; não representa 88 módulos distintos. Esta auditoria não executou outra rodada do jogo nem alterou o código de produção.

## Arquitetura encontrada

| Área | Implementação atual | Lacuna para o master plan |
| --- | --- | --- |
| Entrada Forge | `AlienClient` carrega configuração, registra eventos e instancia diretamente HUD, mundo, cosméticos e desempenho | Uma única composição; não há seleção de edições no build da base auditada |
| Eventos | Buses públicos do Forge/FML e assinaturas `@SubscribeEvent` | Não há abstração própria de ciclo de vida dos módulos; reutilizar os eventos existentes é suficiente, sem recriar obrigatoriamente outro event bus |
| Módulos | Opções concretas em POJOs; linhas da GUI e labels de `HudModules` montadas por código | `HudModules` não é um Module Manager; faltam registro tipado, metadados, dependências, ciclo de ativação/desativação e compatibilidade por módulo |
| Configuração/perfis | `ConfigStore` usa JSON versionado, normalização, gravação atômica, import/export e associação por servidor | Todas as opções são visuais e compartilham um esquema; não há namespace/política de ativação específica de Full |
| Renderização | `CosmeticManager`, malhas/cache/capa e `WorldManager`; restauração de handlers e estado OpenGL | Existem motores especializados, não uma API geral única de Render Engine; preservar os caminhos que já funcionam ao introduzir interfaces |
| Interface/temas | Panels/Studio, colunas, busca, RGB, transparência, animações e temas JSON | Catálogos e controles são concretos; fonte padrão, sem blur, tipografia própria, pacote de ícones ou edição geral de gradientes/RGBA |
| Atalhos | Keybinding configurável para abrir o menu | Não há Keybind Manager com bindings independentes de cada módulo |
| Desempenho | Orçamentos locais, distância, cache e janela de 240 amostras de frame time | Não há benchmark vanilla/Forge/Alien, controle geral de memória ou otimização comprovada de toda a renderização vanilla |
| Build/teste | Um JAR principal e `sourceSets.smoke` separado, com classes de teste excluídas do mod | Não há dois artefatos Legit/Full ou prova de exclusão física de módulos Full na base auditada |

Não foram encontrados módulos de combate, automação de inventário, alteração de alcance/velocidade ou ESP na produção 0.2.0 auditada. As alterações de orientação em `CosmeticStudioScreen` servem à prévia 3D e são restauradas após desenhar; não constituem Aim Assist. A leitura de movimento em cosméticos decide efeitos visuais e não altera velocidade. Não há registro próprio de pacotes ou coleta de credenciais no fluxo dos módulos revisados.

## Classificação frente ao plano Legit

As classificações são aplicadas ao escopo indicado em cada linha. Uma implementação parcial de uma área não significa que todos os itens daquela área existam.

| Recurso solicitado | Classificação | Constatação e próximo incremento |
| --- | --- | --- |
| Forge, Right Shift, configuração, perfis e import/export | Implementado e testado | Preservar a base; estender para metadados/estados de módulos sem perder perfis existentes |
| ClickGUI premium | Implementado parcialmente | Panels/Studio e componentes funcionam; acabamento solicitado, RGBA completo, blur, tipografia moderna, ícones e gradientes gerais ainda exigem implementação/avaliação |
| FPS, CPS, teclas, ping, coordenadas, armadura, inventário, relógio, texto e editor de HUD | Implementado e testado | Renderizadores/editor possuem cobertura da rodada existente; não há teste individual de todos os possíveis estados de dados/input |
| Marca Alien e lista de visuais ativos | Implementado e testado | São widgets informativos derivados das configurações; não são um catálogo de módulos Full |
| Potion Status, crosshair, Target HUD, nametags, scoreboard, hotbar, block outline e zoom | Planejado | Não estão implementados na base auditada |
| Toggle Sprint, Toggle Sneak, item/sword animations e HitFX/Hit Particles | Planejado | Não confundir partículas de movimento/aterrissagem ou animação do charm com eventos de acerto ou animações específicas de itens; avaliar a classificação de recursos que automatizam input |
| Fog Editor, Visual Weather e Time Visual Override | Implementado e testado | São locais; horário visual não altera a iluminação real do terreno; Storm não cria raio/trovão/áudio próprio |
| CustomSky Ultimate | Implementado parcialmente | Há panorama equiretangular PNG, gradiente, estrelas, discos de sol/lua, aurora/nebulosa procedurais e paletas; cubemaps, formato OptiFine Custom Sky, texturas customizadas de sol/lua e transições cinematográficas não existem |
| Kasa, Kitsune e capa animada de referência | Implementado e testado | Malhas originais, UVs/emblemas da capa e prévia funcionam; a avaliação de acabamento final continua necessária, sem equiparar automaticamente a qualidade dos produtos citados |
| Kabuto, coroa, visor/óculos, mochila, asas, charm e efeitos | Implementado e testado | Catálogo existente preservado; a capa usa deformação procedural, não física de tecido/colisões |
| Oni, orelhas/caudas, acessórios flutuantes e catálogo amplo adicional | Planejado | Não estão presentes |
| Importação de cosméticos | Implementado parcialmente | JSON limitado a caixas e PNG; não importa malhas arbitrárias OBJ/glTF/Blockbench ou animações externas |
| Inspector por UUID e presets | Implementado parcialmente | Overrides locais e perfis exportáveis existem; compartilhamento online e catálogo remoto não existem; não há validação visual com múltiplos jogadores remotos nesta evidência |
| Performance Engine completo | Implementado parcialmente | Competitive/Balanced/Cinematic limitam os efeitos Alien; FPS/frame time/P95/memória são diagnósticos, não prova de melhoria frente a vanilla/Forge |
| Motion blur, pipeline de pós-processamento e shaders próprios | Planejado | Filtro de cor atual é sobreposição simples; partículas são formas planas voltadas à câmera |
| OptiFine e conflitos com outros mods | Não validado | Há análise dos pontos de integração, mas nenhuma rodada real de compatibilidade com OptiFine; manter recursos proprietários fora do repositório |
| Servidor privado autorizado, launcher externo, áudio, outras GPUs/OS | Não validado | Mundo integrado foi executado; isso não comprova as demais plataformas ou ambientes |
| Presets com os seis nomes Legit/Full do novo plano | Planejado | Presets atuais de desempenho e perfis livres não são esses seis modos de edição |

## Full: planejamento explícito, sem módulos fictícios

Todos os módulos abaixo estão **planejados, não implementados** na base auditada. A futura edição Full pode reutilizar os visuais Legit, mas uma categoria/menu, uma interface Java ou um registro de metadados não constitui a implementação do comportamento.

| Categoria Full | Módulos a avaliar | Limites que devem constar nos metadados/testes |
| --- | --- | --- |
| Combat | Aim Assist, AutoClicker, Reach, Velocity, Hit Select, Auto Sprint, Target Selection, Combat Indicators, Attack Timing Tools | Distinguir indicadores de automação/alterações de input e física; nenhuma aceitação de alcance, knockback ou acerto por servidor pode ser presumida |
| Movement | Sprint Controls, Movement Helpers, Flight, Speed Controls, Step Assist, NoSlow, Movement Automation, Parkour Assistance, Movement Indicators | Definir cenários locais/privados e mecânicas 1.8.9 para cada módulo; o servidor continua podendo corrigir/rejeitar movimentos |
| Player | Inventory Management, Auto Armor, Auto Tool, Hotbar Utilities, Item Management, Player Indicators, Action Helpers, Automation Profiles | Separar renderização informativa de cliques e ações reais de inventário; respeitar o modelo de slots e transações de 1.8.9 |
| Render & Awareness | ESP, Tracers, Entity Highlights, Custom Nametags, Target/Distance/Visibility Indicators, Entity Filters, World Markers | Identificar informação normalmente oculta; usar somente dados disponíveis ao cliente, sem prometer conhecimento de entidades/chunks não recebidos |
| World & Utility | Waypoints, World Markers, Inventory Utilities, Auto Tool, Chest Indicators, Custom Brightness | Planejar cada implementação e sua classificação; Visual World Controls, Profile Switching e Config Import/Export já possuem uma base visual compartilhável, sem implicar os demais módulos Full |
| Infraestrutura | Module Keybinds, configurações individuais, aviso de impacto, compatibilidade e política de ativação | Precisa de implementação própria e testes; na Full, módulos que alteram jogabilidade devem iniciar desativados e não ser reativados automaticamente por perfil de servidor |

**Incompatível com 1.8.9 vanilla:** AutoTotem moderno, Totem of Undying e automação baseada no slot de mão secundária, pois esses itens/slots não existem nessa versão. Não criar controles fictícios para eles. Flight/Speed/Reach não são classificados como impossíveis pela versão; são recursos ainda não implementados, cuja aceitação depende das regras e validação do ambiente de teste.

Evasão de anticheat, falsificação de identidade, roubo de sessões/credenciais e exploração de servidores ficam fora do escopo solicitado. Nenhum módulo deve prometer ser indetectável. Recursos exclusivamente visuais também podem ter restrições nas regras do servidor; a descrição deve informar o comportamento real, sem declarar autorização universal para Legit.

## Separação de fontes recomendada

O primeiro incremento deve criar uma fronteira de build preservando o código 0.2.0. Não apagar nem reimplementar os motores atuais para obter dois nomes de arquivo.

```text
src/main/java/        base compartilhada existente, preservada inicialmente
src/legit/java/       composição/registro exclusivo da edição Legit
src/legit/resources/  metadados e seleção de provider Legit
src/full/java/        composição Full e, futuramente, módulos avançados
src/full/resources/   metadados e seleção de provider Full
src/test/java/        regressões da base
src/legitTest/java/   catálogo e artefato Legit
src/fullTest/java/    catálogo, ativação e comportamento Full
src/smoke/java/       teste gráfico existente, preservado
```

É aceitável começar com source sets no mesmo projeto ForgeGradle para reduzir risco, mantendo as fontes atuais em `main`. Se a separação crescer, converter para projetos Gradle independentes com bibliotecas compartilhadas. Em ambos os casos, o grafo de compilação Legit não pode depender das fontes/classes/resources Full; Full depende da base, nunca o inverso.

Proposta de composição:

1. Extrair contratos pequenos e testáveis: `EditionProvider`, `Module`, `ModuleDescriptor`, `ModuleRegistry`, tipos de configuração e compatibilidade. Descrever ID estável, categoria, impacto visual/input/física/informação, versões suportadas, disponibilidade, motivo e estado inicial.
2. Manter a entrada Forge comum, que recebe um único provider de edição presente no artefato. Um mecanismo como `ServiceLoader` permite compor providers exclusivos sem importar classes Full na base. Exigir exatamente um provider e rejeitar metadados ambíguos.
3. Adaptar os motores atuais ao registro e ciclo de vida; desativar um módulo deve restaurar handlers/estado e remover suas inscrições quando aplicável. Não registrar o mesmo listener repetidamente ao abrir a GUI.
4. Construir a GUI a partir do catálogo realmente carregado. Legit recebe somente módulos visuais/permitidos pela sua política; Full acrescenta seu catálogo. Não incluir módulos Full desativados nem apenas escondê-los na Legit. Itens planejados pertencem à documentação, não a botões que aparentam funcionar.
5. Versionar a política de perfis e separar o armazenamento das edições. Importar um perfil Full na Legit não pode carregar classes, opções ou ativações Full. Ao carregar perfil ou entrar em multiplayer, a Full deve manter módulos de vantagem competitiva desativados até uma ação explícita do usuário, com descrição/aviso correspondente.
6. Produzir `AlienClient-Legit-1.8.9.jar` e `AlienClient-Full-1.8.9.jar` por tarefas separadas, incluindo somente seus outputs e resources autorizados. Não usar um único JAR universal com seletor de funções ocultas.

Antes de declarar a separação concluída, compilar as duas variantes com seus classpaths isolados e inspecionar os arquivos finais. Testes devem afirmar a ausência de classes, registros, providers, configurações e recursos exclusivos Full no JAR Legit, além da presença do provider correto. Uma simples busca textual por nomes ou um botão escondido não prova isolamento. O conjunto aprovado de classes por edição deve ser revisável; nenhuma flag runtime torna segura a inclusão indevida de implementação Full.

## Fases e critérios de saída

| Fase | Incremento concreto | Evidência exigida antes de avançar |
| --- | --- | --- |
| 1 — Auditoria e arquitetura | Preservar a base; introduzir fronteiras de fontes/providers e tarefas separadas; iniciar catálogos reais | Regressões existentes preservadas; dois classpaths/artefatos inspecionados; nenhuma funcionalidade Full inventada |
| 2 — Visual overhaul | Evoluir componentes, busca, RGBA, temas, tipografia e layouts sobre a interface existente | Interação real, persistência e capturas no jogo em ambas as composições; sem assets proprietários copiados |
| 3 — Sky/Cosmetics | Incrementar importadores, recursos cinematográficos próprios, UV/animações e os três cosméticos de referência | Validação de formatos/limites e framebuffer; reversão dos handlers; medir custo real |
| 4 — Legit | Adicionar os módulos visuais/HUD ausentes em grupos pequenos, com opções e limites | Cada módulo executado e classificado; pacote sem implementações Full; perfis migrados sem perda |
| 5 — Infraestrutura Full | Categorias, registro, keybinds, avisos, configuração isolada e padrões seguros | Full inicia independentemente; opções que alteram jogabilidade não ativam por login ou importação |
| 6 — Módulos Full | Implementar comportamentos compatíveis um por vez, começando em mundos locais | Testes do comportamento real e documentação de efeitos locais/validação do servidor; servidor privado somente quando autorizado e disponível |
| 7 — Performance/QA | Compatibilidade, estabilidade e comparativo reproduzível | Resultados medidos, ambiente e cenários registrados; manter falhas, skips e itens não validados distintos |
| 8 — Release | Empacotar cada variante, fontes, documentos, hashes e capturas reais | Ambos os JARs compilados/reofuscados/inspecionados e os testes aplicáveis concluídos; nenhuma promessa sobre servidores públicos sem evidência |

A conclusão desta auditoria é que a 0.2.0 oferece uma base visual real e testada para Legit, mas não implementa o master plan completo nem a Full. A separação de fontes e contratos é o próximo trabalho concreto. Seu resultado deve ser registrado por compilação, inspeção e testes novos, sem reapresentar a evidência 0.2.0 como prova de duas edições já entregues.
