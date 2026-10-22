# Desenvolvimento

## Estrutura

- `AlienClient`: ciclo de vida do Forge, keybinding, troca opcional do menu e contexto de servidor.
- `config`: POJOs versionados, normalização, gravação atômica, backup, perfis e associações.
- `gui`: layouts Panels/Studio, componentes, temas e menu inicial; `PanelLayoutConfig` persiste posições proporcionais e estado recolhido das colunas.
- `hud`: doze tipos de widgets informativos, editor proporcional à resolução e preset compacto; `HudModules` deriva a lista de visuais das configurações locais habilitadas.
- `cosmetic`: camadas de jogador, estúdio com prévia girável/zoom, cache/importação e Inspector por UUID.
- `cosmetic.mesh`: malhas originais trianguladas com normais/sombreamento; `CapeCloth` atualiza em memória uma grade curva reutilizada, com UVs presos ao tecido.
- `world`: handlers de céu/clima, panorama, partículas ambientes/movimento, paletas Crystal/Sakura/Nebula, neblina, câmera e filtro de cor.
- `performance`: orçamentos, presets e amostras reais de tempo entre frames.
- `src/test`: regressões JUnit que não precisam abrir o jogo.
- `src/smoke`: verificações gráficas separadas, quando disponíveis; não fazem parte do JAR distribuído.

Os renderizadores consultam a configuração atual; carregar um perfil não deixa referências antigas permanentes. Toda a execução dos módulos ocorre no cliente. O mod não registra pacotes, não altera velocidade/alcance/dano e não escreve horário/clima do mundo.

## Geometria e estado gráfico

`CosmeticMeshes` constrói uma vez as malhas integradas de chapéus, máscaras e acessórios. `CosmeticMeshRenderer` reutiliza o desenho estático por malha e cores, com cache limitado. A capa segue outro caminho: `CapeCloth` mantém arrays de posições/normais e os deforma conforme fase, deslocamento local e intensidade escolhida. Não é uma simulação física de tecido ou colisões. Low/Balanced/High escolhe a resolução dessa grade, e o preset Competitive usa a versão Low. Desativar as animações interrompe a variação temporal.

`CapeTextures` gera tecido, acabamento e emblemas originais em texturas locais reutilizáveis. Os UVs permanecem ligados à malha durante a deformação. Essa geometria integrada é independente de `CosmeticAssets`, cujo importador compatível continua aceitando somente caixas JSON e PNG com limites de tamanho.

`ParticleShapes` mantém triângulos reutilizáveis para Diamond, Petal e Spark. O renderizador orienta cada forma para a câmera, aplica rotação/fade e preserva teste de profundidade. São partículas planas, não objetos volumétricos ou shaders. `AtmospherePresets` configura explicitamente céu, horário visual, neblina, clima visual e efeitos; não altera o estado real do mundo.

O Minecraft 1.8.9 não chama o handler normal do céu abaixo de quatro chunks. `WorldManager` oferece um caminho em `RenderWorldLastEvent` para preencher somente o fundo de profundidade nessa situação. Preserve também `GL_VIEWPORT_BIT` ao alterar `glDepthRange`: o intervalo de profundidade pertence a esse grupo, não apenas a `GL_DEPTH_BUFFER_BIT`. Restaurar a matriz, a ligação de textura e o estado acompanhado por `GlStateManager` evita que o próximo renderizador receba caches inconsistentes.

## Configuração e compatibilidade de perfis

O mod 0.2.0 mantém a versão JSON **1**. Construtores e normalizadores completam campos ausentes: escolha Panels/Studio, layout das colunas, elementos novos do HUD, emblemas/movimento da capa e forma/cores de partículas. Preserve widgets já posicionados pelo usuário; o preset compacto é uma ação explícita. Temas JSON trocam aparência básica, enquanto perfis completos transportam layout e configurações dos módulos.

## Cadeia de build

As versões antigas são intencionais: ForgeGradle 2.1 e Gradle 2.14.1 correspondem a Forge 1.8.9. O wrapper moderno apenas inicia e verifica a distribuição fixada; ele não muda a versão do Gradle que executa o projeto. `build` compila Java 8, roda JUnit, empacota e reofusca o mod. As dependências do Minecraft e do Forge ficam fora do JAR produzido.

O ForgeGradle legado usa HTTP para recursos do jogo. O script de compatibilidade substitui o transporte por HTTPS para os mesmos arquivos oficiais e verifica o SHA-1 e tamanho definidos no manifesto original. A assinatura dos pacotes, TLS e verificações de integridade não são desativados.

Atrás de proxy, `scripts/gradle-env.sh` configura os parâmetros Java de proxy e um truststore local com as raízes do JDK e os certificados públicos providos pelo sistema. Não copia credenciais para os scripts. As ferramentas ficam em `.tools/` ou no diretório indicado por `ALIEN_TOOLS_DIR`; dependências, mundos de teste, recursos e logs são ignorados pelo Git.

## Ambiente na nuvem

Cada tarefa já tem isolamento. Use o checkout existente em `/workspace/Alien-Client`; não crie outro worktree por padrão. O snapshot retém ferramentas e caches, mas processos gráficos precisam iniciar novamente. Não há serviço backend obrigatório nem chave de API para usar o mod.

O build exige acesso aos repositórios oficiais do Forge, Gradle, Maven, Temurin e Minecraft. O bootstrap e os scripts de build descrevem os destinos reais. Publicar um rascunho de configuração é uma ação separada de compilar e testar nesta instância.

## Testes

```bash
./gradlew test
./gradlew build
```

Para o cliente real e o teste gráfico separado:

```bash
# Sessão gráfica já disponível
./gradlew runClient

# Ambiente de nuvem preparado, com display descartável
env -u DISPLAY ALIEN_TOOLS_DIR=/workspace/.alien-tools ./scripts/smoke-client.sh
```

O smoke usa as classes atuais de desenvolvimento e o source set `smoke`, excluído do JAR distribuído. Cria configurações e mundo descartáveis em `build/smoke-runtime`; não aponte esse teste para a instalação normal de um usuário. Verifique o comando, o cabeçalho de `build/smoke/result.txt`, a ausência de linhas FAIL e as capturas reais do framebuffer. A automação de controles/eventos não equivale a apertar uma tecla física, e execução por software não é benchmark de GPU.

As regressões cobrem persistência/importação de perfis, isolamento por servidor, limites de JSON, recuperação de dados inválidos, temas, valores de HUD/cosméticos/efeitos e decodificação de panoramas. Mantenha testes de geometria/dados independentes do OpenGL quando possível e verifique o resultado visual no jogo para deformação da capa, orientação de partículas e preservação do terreno. Resultados e contagens atuais pertencem a [VALIDATION.md](VALIDATION.md); testes de dados não substituem a execução gráfica, conectividade com servidores ou um comparativo de desempenho.

Evite adicionar transformadores de bytecode como primeira opção. Prefira eventos públicos do Forge e restaure handlers/estado gráfico ao desativar um módulo. Recursos importados devem permanecer limitados, locais e sem execução de código. Novas configurações precisam de padrões e normalização para perfis anteriores.
