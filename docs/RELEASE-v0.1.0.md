# Alien Client 0.1.0 — Forge 1.8.9

Pré-release inicial para testes. Mod de cliente para **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**.

## Download e instalação

**[BAIXAR ALIEN CLIENT 0.1.0.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.1.0/downloads/alien-client-0.1.0.jar?raw=true)**

[Código-fonte completo ZIP](https://github.com/AttackAlien/Alien-Client/archive/refs/tags/v0.1.0.zip) · [Capturas reais](https://github.com/AttackAlien/Alien-Client/blob/v0.1.0/captures/README.md) · [SHA-256](https://github.com/AttackAlien/Alien-Client/blob/v0.1.0/downloads/SHA256SUMS.txt)

1. Baixe **alien-client-0.1.0.jar** pelo botão acima.
2. Instale o Forge 1.8.9-11.15.1.2318 e selecione Java 8.
3. Coloque o JAR na pasta `mods` da instalação e abra o jogo.
4. Pressione **Right Shift** dentro de um mundo para abrir o menu Alien.

O código-fonte ZIP contém código, scripts e testes. O binário compilado está versionado na pasta `downloads` do repositório, com SHA-256. Os anexos nativos da release não puderam ser enviados devido a uma falha de autenticação desta integração no domínio de uploads do GitHub; o botão acima baixa o mesmo JAR verificado diretamente do repositório.

## Incluído

- Menu inicial opcional, painel de módulos, temas RGB e transparência.
- HUD configurável com arraste, escala, alinhamento e dez tipos de elementos.
- Acessórios 3D procedurais, editor com prévia, Inspector por UUID e modelos JSON de caixas.
- Céus procedurais e PNG 360°, neblina, horário e precipitação visuais locais.
- Partículas, efeitos opcionais de câmera/cor, orçamentos e perfis de desempenho.
- Perfis completos, importação/exportação e associação por servidor.

## Validação executada

**46 testes JUnit passaram**, sem falhas, erros ou ignorados. **54 verificações gráficas passaram** executando Minecraft/Forge real em um mundo local descartável, com **16 capturas do framebuffer**. Os testes incluem céu em dois/quatro chunks com terreno preservado, PNG importado, HUD, cosméticos, perfis e encerramento pela ação vanilla Save and Quit.

O teste gráfico usa as classes atuais de desenvolvimento, Xvfb/OpenGL por software e limite de 30 FPS. O JAR foi compilado e reofuscado; sua instalação em launcher externo ainda não foi validada. Os números do HUD não constituem benchmark. O mod não incorpora classes do Minecraft, Forge ou OptiFine.

## Limitações conhecidas

- Pré-release: não há resultado de compatibilidade com OptiFine nem comparativos vanilla/Forge/Alien.
- Áudio, autenticação/multiplayer público, outros sistemas operacionais e GPUs reais ainda não foram validados.
- Céu/clima personalizado restrito ao Overworld. Storm é chuva visual intensa, sem raios/trovões próprios. Iluminação e condições reais do servidor permanecem reais.
- Sem compartilhamento de cosméticos, alteração de skins, OBJ/glTF/Blockbench, importação por URL/ZIP, editor específico de animação de itens, texturas personalizadas de sol/lua ou pipeline de shaders.

Código-fonte e instruções estão na branch `main`. Galeria: [captures/README.md](https://github.com/AttackAlien/Alien-Client/blob/main/captures/README.md). Os detalhes de escopo estão em [docs/FEATURES.md](https://github.com/AttackAlien/Alien-Client/blob/main/docs/FEATURES.md).
