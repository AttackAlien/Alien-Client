# Validação da versão 0.2.0

Executada em 21 de outubro de 2026, no Linux x86_64 do ambiente de nuvem, com Temurin 8u472, Gradle 2.14.1, ForgeGradle 2.1 e Forge 1.8.9-11.15.1.2318/stable_22. A validação anterior está no [tag 0.1.0](https://github.com/AttackAlien/Alien-Client/blob/v0.1.0/docs/VALIDATION.md).

## Compilação e testes

**76 testes JUnit passaram, sem falhas, erros ou ignorados.** Cobrem configuração/perfis e migração, posições dos painéis, HUD e módulos ativos, temas, geometria das malhas, deformação/UV/normais da capa, texturas originais, limites dos cosméticos, importações e presets de céu/partículas.

`build` concluiu com sucesso usando Java 8 e dependências reais. O JAR é reofuscado pelo ForgeGradle; os testes gráficos permanecem em um source set separado e não integram o mod. O SHA-256 do binário publicado está em [downloads/SHA256SUMS.txt](../downloads/SHA256SUMS.txt).

## Minecraft/Forge real

**88 verificações passaram**, produzindo **35 PNGs originais do framebuffer** em um mundo integrado descartável. [Galeria](../captures/README.md) · [Resultado original](../captures/v0.2.0/result.txt) · [Hashes dos PNGs](../captures/v0.2.0/SHA256SUMS.txt).

Foram confirmados:

- Descoberta da versão 0.2.0 pelo Forge e menu inicial opcional.
- Carregamento das classes atuais de desenvolvimento, excluindo JARs antigos.
- Atalho real Right Shift abrindo os painéis flutuantes dentro do mundo.
- Controles de aparência, desempenho, HUD, céu, efeitos e cosméticos alterando configurações reais.
- Prévia 3D com rotação/zoom, chapéu curvo, Kabuto/Kitsune, asas e capa texturizada.
- Movimento visível da capa: 12 quadros com intervalo mínimo de 100 ms; até **132 pixels alterados** na região restrita ao tecido. [Métrica](../captures/v0.2.0/cape-motion-metrics.txt).
- Céu noturno em quatro e dois chunks, preservando terreno e jogador.
- Panorama PNG importado desenhado e liberado ao voltar ao céu procedural.
- Crystal cyan e Sakura rosa visivelmente diferentes no framebuffer, com HUD e partículas reais.
- Horário do servidor avançando normalmente e estados reais de chuva/trovão preservados.
- Renderizadores vanilla restaurados ao desativar céu/clima locais; OpenGL sem erros.
- Perfis salvos/carregados e mundo encerrado pela ação vanilla Save and Quit, retornando ao menu inicial.

O GIF da galeria só reúne os 12 PNGs originais da capa, com paleta GIF e reprodução a 8 quadros/s, sem interpolação. Os PNGs não receberam retoques.

## Comandos utilizados

Ferramentas já preparadas em `/workspace/.alien-tools`:

```bash
cd /workspace/Alien-Client
ALIEN_TOOLS_DIR=/workspace/.alien-tools ./gradlew --no-daemon build
env -u DISPLAY ALIEN_TOOLS_DIR=/workspace/.alien-tools ./scripts/smoke-client.sh
```

O script abre Xvfb, configura OpenGL por software, executa `./gradlew --no-daemon runSmokeClient`, valida `build/smoke/result.txt` e encerra a tela de teste. O jogo usa `build/smoke-runtime/`; as evidências ficam em `build/smoke/`. Para jogar em uma sessão gráfica local: `./gradlew runClient`.

## Falhas encontradas e corrigidas

O primeiro build registrou quatro falhas de expectativa dos testes antigos do HUD: a revisão passou de dez para doze widgets. Os testes foram atualizados para também cobrir os novos elementos e a migração dos layouts existentes; a execução final passou.

A primeira execução gráfica da revisão encontrou um crash ao consultar `GL_SCISSOR_BOX`: LWJGL 2 exige buffer de 16 inteiros mesmo para uma propriedade com quatro componentes. O editor passou a reservar o tamanho exigido e restaurar os quatro componentes válidos. A revisão visual também encontrou sobreposição do HUD nos títulos dos painéis; o HUD comum agora fica oculto nos editores Alien, que mantêm suas próprias prévias. A execução seguinte passou as 88 verificações e encerrou normalmente.

## Limitações da evidência

O teste gráfico executa as classes de desenvolvimento atuais, com limite de 30 FPS e Mesa llvmpipe por software. Não é um benchmark e não mede comparativos vanilla/Forge/Alien. Nebula possui cobertura de configuração, mas não ganhou uma captura específica nesta rodada.

O JAR foi compilado/reofuscado; instalação em launcher externo, áudio (sem dispositivo disponível), autenticação/multiplayer público, OptiFine, outros sistemas operacionais e hardware gráfico real ainda não foram validados. O jogo usou skin padrão. [Funcionalidades e pendências](FEATURES.md) · [Protocolo de benchmark](BENCHMARKS.md).
