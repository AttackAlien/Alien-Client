# Scene Studio — referência do TikTok e implementação 0.5.0-alpha.1

O vídeo público [Dev log #10, @pickzugng / Purxiy](https://www.tiktok.com/@pickzugng/video/7700173939602967841), indicado pelo usuário em https://vt.tiktok.com/ZSg9eh38j/, foi acessado e analisado quadro a quadro em 24/10/2026. Duração 30,77 s; nove cartões de funcionalidades. O vídeo original fica apenas no diretório temporário de análise, não é redistribuído como mídia do Alien.

## Versão e preservação

Mantido **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**. Os efeitos usam GLSL 1.20 e hooks existentes do Forge; não exigem biomas/blocos ou protocolo de uma versão moderna. O cenário com cerejeiras do vídeo não é um dos nove módulos demonstrados. Não foi necessária migração para implementar as funções visuais identificadas. A página pública Purxiy descreve Fabric/Sodium/Iris, sem confirmar uma versão exata do Minecraft publicamente; isso não determina o requisito técnico do Alien.

Branch `feature/visual-scenes-20261024`, baseada no PR #16, mantendo os PRs #15/#16, HUD/Full, cosméticos, perfis e Performance Engine. Novo número de versão do client: **0.5.0-alpha.1**, sem mudar a versão do jogo. Nenhum merge na main. Os efeitos novos começam desligados em perfis antigos e novos.

## Os nove recursos

| Cartão do vídeo | Implementação real no Alien | Limites concretos |
| --- | --- | --- |
| Shockwave | Detecta salto/pouso real e desenha uma onda expansiva no terreno visível por reconstrução de profundidade. Cor, força e raio configuráveis. | Máximo quatro ondas, sem alterar blocos ou física. |
| Cape | Reutiliza a capa curva com inércia/material CC0, acrescentando brilho animado no tecido. | Emblemas e arte Alien; não inclui logo/capa Purxiy. Animação depende do switch de animações existente. |
| Trail | Ativa a fita luminosa real do histórico do jogador, com duas cores/fade e configurações já existentes. | Distância, expiração, teleporte e limites do Performance Engine preservados. |
| Cute World | Arte original com olhos grandes, brilho, bochechas e sorriso sobre o rosto dos jogadores, incluindo piscar. | Aplica-se a jogadores visíveis; não substitui todos os blocos/mobs/texturas do mundo. |
| Cyberspace | Composição real de grade e trilhas de circuito em coordenadas do mundo, incluindo grade no céu; altera também o material visual dos jogadores/itens na cena. | É um efeito de tela com profundidade, não substituição de cada modelo/textura por um asset externo. |
| X-Ray | Material translúcido com esqueleto decorativo, crânio, costelas, juntas e membros seguindo as poses vanilla. | É visual de ossos de jogadores, como no vídeo; mantém o teste de profundidade do terreno. Não é busca de minérios. |
| Glass | Refração de tela na geometria real dos jogadores, itens segurados em terceira pessoa e itens soltos no mundo, mais contorno iluminado. | Refração de tela, sem ray tracing/PBR. A mão/item em primeira pessoa e ícones do inventário permanecem vanilla. Armadura vanilla é ocultada nos modos de material para deixar o corpo visível; volta no modo Natural. |
| Chroma | Desatura o mundo distante e devolve suas cores reais em torno do jogador e do caminho percorrido, com histórico limitado/expiração. | Efeito local; não altera chunks, arquivos, regras ou pacotes. |
| Sky | Oito céus procedurais animados: Cosmic, BlackHole, Aurora, SnowClouds, Fire, Sunset, Ocean e Storm. | O vídeo não nomeia os oito presets; estes são presets originais Alien, não cópias de imagens/shaders Purxiy. |

Cute World, Shockwave e Sky podem combinar-se com um material selecionado. Cyberspace, Skeleton, Glass e Chroma são seleções alternativas de material/composição, como nas cenas individuais da referência. Cape e Trail continuam no sistema de cosméticos existente, com seu próprio master.

## Uso

**Effects Studio → Scene Studio** abre os nove controles. O master Scene FX controla os efeitos de cena; Cape e Trail usam o master Cosmetics e ficam disponíveis independentemente dele. A tela mostra o estado real do compilador de shaders. **Cosmetic Studio → Motion → Shine** permite desligar o novo brilho mantendo a capa. Todos os valores são salvos nos perfis Unified, incluindo cópias/looks da capa.

## Renderização e compatibilidade

O compositor copia a cor/profundidade do framebuffer antes de processá-las, preserva estado OpenGL e roda antes do HUD. As texturas são reutilizadas e redimensionadas com a janela; uniforms e buffers têm cache. O histórico Chroma tem 32 amostras, ondas são limitadas a quatro e materiais/rostos respeitam distância/orçamento. Troca de perfil/mundo, OFF e desconexão limpam o estado transitório. Os renderizadores de itens delegam ao renderizador anterior quando o efeito está desligado.

Os materiais de jogadores usam animações/poses vanilla, com renderização alternativa limitada aos modos escolhidos. Invisibilidade e oclusão pelo terreno permanecem. Modelos, skins e dados de inventário continuam reais; nenhum destes efeitos muda ataques, alcance ou controle de combate.

É necessário suporte a framebuffer e shaders OpenGL; em hardware sem suporte a tela informa a indisponibilidade e o renderizador nativo continua. Não há promessa de compatibilidade com OptiFine ou outros renderizadores/mods não testados. Não há benchmark comparativo de FPS.

## Validação

Cinco novos testes JUnit cobrem configuração legada, normalização de uniforms, limites/expiração de amostras, perfis reais e arte/piscar. O mod isolado `src/sceneSmoke` executa as nove funcionalidades em Forge real, verifica shaders vinculados, salto/pouso, materiais separados de jogador/item segurado/item solto, controles reais, Chroma em movimento, oito cores de céu diferentes, animação entre framebuffers, OFF, desconexão e ausência de alteração do horário do servidor.

Uma verificação mais estrita detectou que o primeiro item solto da fixture não havia entrado no mundo antes do carregamento do chunk após teleporte. A fixture agora carrega o chunk e confirma a criação real no servidor antes de verificar o render de vidro de cada tipo; a falha não foi ignorada. As primeiras capturas também motivaram correções na grade horizontal e na densidade de estrelas.

Resultados finais, comandos e mídia são publicados em `VISUAL-SCENES-VALIDATION.md`. Nenhum logo, shader, modelo, binário ou textura proprietário Purxiy foi incluído. As implementações e arte novas são originais; o tecido CC0 anterior e seu crédito foram mantidos.
