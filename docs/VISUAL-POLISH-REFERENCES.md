# Cosméticos e trilhas — fontes e implementação 0.4.0-alpha.4

Branch `feature/cosmetic-trails-polish-20261023`, derivada da revisão `e86870479bfe0af25e5ca32eb62a1848865af699` do PR #15. Preserva HUD Ultimate, Full, perfis, Performance Engine e redesign 0.4. Sem merge na main. A revisão acrescenta melhorias visuais locais; não acrescenta protocolo multiplayer, biblioteca moderna ou download de assets durante o jogo.

## Pesquisa antes das alterações

Foram lidos CosmeticManager, CosmeticStudioScreen, CosmeticConfig, CosmeticMeshes/MeshRenderer, CapeCloth, CapeTextures e o emissor de partículas de mundo. A capa já tinha uma malha curva com ondas periódicas, mas não tinha resposta amortecida independente por jogador. O kasa já era circular; sua superfície tinha poucos detalhes. A trilha dos cosméticos ainda chamava a partícula vanilla REDSTONE, separada do Particle Studio.

| Fonte consultada | Licença / compatibilidade verificada | Uso nesta revisão |
| --- | --- | --- |
| [WaveyCapes](https://github.com/tr7zw/WaveyCapes) / [licença](https://github.com/tr7zw/WaveyCapes/blob/main/LICENSE) | tr7zw Protective License, com restrições comerciais. | Referência conceitual de capa flexível. Nenhum código, binário ou asset foi copiado. A resposta amortecida foi escrita para o renderizador existente. |
| [three.js LineGeometry](https://github.com/mrdoob/three.js/blob/dev/examples/jsm/lines/LineGeometry.js) | MIT; JavaScript/WebGL, não é uma dependência Forge. | Referência de representação de uma linha por posições encadeadas. RibbonPath/renderer são implementação original Java/LWJGL, sem incluir three.js. |
| [Luna’s Cosmetics](https://github.com/AttackStudios/lunascosmetics) | MIT; Fabric 1.21.11 / NeoForge 1.21.1, Java 21. | Referência de variedade de headwear, incluindo Sakura Crown. A coroa desta revisão usa pétalas curvas e malha original; nenhum modelo, textura ou código desse projeto foi copiado. |
| [PlayerAnimator](https://github.com/KosmX/minecraftPlayerAnimator) | MIT; Minecraft 1.16+; README informa descontinuação. | Avaliado e descartado como dependência incompatível com Forge 1.8.9/Java 8. |
| [Poly Haven — Fabric Pattern 07](https://polyhaven.com/a/fabric_pattern_07), Rob Tuytel | [CC0 1.0 / domínio público](https://polyhaven.com/license). | **Asset realmente reutilizado:** mapa de roughness original 1K em JPEG, usado como detalhe de trama na cor do tecido. Arquivo original sem alteração, incluído no JAR com crédito/proveniência. |

A imagem Poly Haven foi obtida pela [API de arquivos](https://api.polyhaven.com/files/fabric_pattern_07), com tamanho e MD5 conferidos antes de incluí-la. SHA-256 `3d90ac40b5c32a15649cc8525b7eeed133ea46b63a0e3eae5e34911d5c967e3a`. O [NOTICE incluído no JAR](../src/main/resources/assets/alienclient/textures/cosmetic/NOTICE.txt) registra autor, licença e URL. O mapa contribui à textura colorida; não foi implementado render PBR ou normal mapping.

## Mudanças visuais reais

- **Trilha Ribbon:** fita contínua com centro iluminado, gradiente entre as duas cores do cosmético, bordas transparentes e desaparecimento progressivo. Segue o histórico real de movimento, com interpolação Catmull-Rom. Teleportes quebram o histórico; parada expira a fita; invisibilidade, espectador, troca de mundo, desativação ou animação OFF removem o efeito. O estilo Particles preserva a trilha vanilla anterior.
- **Capa:** resposta amortecida ao movimento, mudança de direção e movimento vertical, com estado separado por jogador e prévia isolada. Continua usando a malha com ombros presos e borda arredondada; ondas e dobras se combinam à inércia. Textura de trama CC0 mantém os emblemas Alien/Moon/Stars/Plain e as cores escolhidas.
- **Headwear:** kasa ganha anéis finos de trama; metais/detalhes dos acessórios ganham realce de luz em cache. A opção Sakura acrescenta uma coroa com sete flores, pétalas arredondadas, folhas e aro circular. As seleções anteriores continuam disponíveis.
- **Controles:** Cosmetic Studio → Motion → Trail settings abre estilo, largura, duração e opacidade por jogador. Sliders exclusivos da fita ficam desabilitados no estilo Particles. Os novos valores sobrevivem a perfis, cópias e looks salvos. O botão Trail anterior continua funcionando.

A geometria estática continua em cache; a resposta da capa tem no máximo 64 estados por renderizador de skin e limpa na troca de mundo. Trilhas guardam no máximo 48 pontos por jogador e oito jogadores, limitam segmentos pelo Performance Engine e usam teste de profundidade. Há uma passagem principal e brilho opcional; Competitive/redução de qualidade omite o brilho. Não foi medido um ganho comparativo de FPS.

## Testes e evidência

Cinco novos testes JUnit exercitam expiração/teleporte/limite do histórico, estabilidade da resposta da capa em taxas de render diferentes, independência por jogador e persistência/compatibilidade dos novos campos. A verificação de topologia/normais passou a incluir a coroa Sakura.

O smoke Ultimate foi ampliado com cenas reais de headwear, material da capa, controles e caminhada por input X11, verificando emissão/desenho limitado da fita, resposta da capa, persistência e expiração após parar. A primeira execução passou 91 checks. A suíte foi ampliada para verificar também a câmera frontal em terceira pessoa. A inspeção das imagens identificou pétalas baixas e zoom cortando o topo da coroa; ambos foram corrigidos antes da validação final. Resultados finais e mídia são registrados no relatório desta revisão.
