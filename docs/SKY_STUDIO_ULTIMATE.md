# Sky Studio Ultimate — primeira implementação

**Branch:** `feature/sky-studio-ultimate` (criada da `main`, independente do PR #3).

## Mudanças concretas

- Importação de **cubemap 3×2 em um PNG** terminado em `*.cube.png` com seis faces quadradas (64..1024 px por face) e UVs separados com margem de meio texel.
- Panoramas PNG equiretangulares existentes **continuam suportados** com o mesmo código de projeção.
- Novos controles locais: yaw 0..360°, drift opcional 0..2°/s, brilho de imagem 20..100% e variação suave de luminosidade das estrelas.
- Editor **Sky options** acessível pelo World Studio, com botão Reset para apenas as novas opções.
- Validação do atlas **antes de decodificar a imagem**, mesmo limite de 16 MiB, diretório local e rejeição de caminhos externos.
- Testes JUnit de face/UV/raio, arquivos inválidos e persistência dos valores retrocompatíveis no WorldConfig.
- O renderizador preserva os presets Nebula, Galaxy, Aurora, Japanese, Crystal e Sakura, as partículas e o sistema de clima.
- Apenas componentes Java 8, Forge 1.8.9/LWJGL 2. Nenhuma textura, dependência ou executável externo introduzido.

## Limites da primeira fase

**Não é** um pipeline HDR/EXR, um sistema de shaders ou uma ferramenta de modelagem 3D. Uma imagem `*.cube.png` possui todas as faces numa única textura, não seis arquivos independentes. A orientação do atlas é explícita no [guia de importação](IMPORTS.md).

Os valores default preservam o aspecto anterior. As configurações não mexem no relógio do servidor, na chuva real ou na orientação do jogador.

### Validação recomendada dentro do Minecraft

1. Execute o GitHub Actions do PR #4 em **Legit e Full**. Agora o workflow compila com Java 8, roda JUnit, verifica os JARs e inicia o Forge 1.8.9 em Xorg dummy para testar capturas reais do céu. **O teste só é considerado aprovado quando os dois jobs terminam verdes e apresentam `PASS N checks` no `sky-smoke/result.txt`.**
2. Compare a imagem PNG equiretangular importada antes/depois, com os campos novos em seus defaults.
3. Importe um atlas `meu-ceu.cube.png` (exemplo: 1536×1024), valide cada direção e as costuras.
4. Experimente yaw, drift, brilho e cintilação; confirme que as opções persistem após reiniciar.
5. Ative o céu customizado nos render distances 2 e 4, confirme que os terrenos continuam aparecendo.
6. Teste restauração do céu vanilla, dim. Nether/End, presets e perfil de servidor.
7. Valide a compatibilidade com OptiFine separadamente antes de lançar.

Screenshots antigos no repositório representam o código anterior e não comprovam a atualização.

## Smoke gráfico automatizado (implementado)

O workflow [`sky-studio-ci.yml`](../.github/workflows/sky-studio-ci.yml) usa [`AlienSkySmoke.java`](../src/skySmoke/java/dev/alien/client/smoke/AlienSkySmoke.java), uma fonte de teste que **não entra nos JARs de distribuição**. Ele abre um mundo superflat **descartável** e grava screenshots e um resultado explícito em `build/<edition>/sky-smoke/`.

O smoke gera um PNG 3×2 de seis faces com cores distintas e valida amostras reais do framebuffer mirando **Up, South e North**, a rotação de 180°, escurecimento da imagem, persistência das novas opções, botões de `Skybox Options`, retorno ao procedural e, depois, ao céu vanilla. Os seis lados são cobertos pela matemática/UV dos testes JUnit; as demais três direções e as costuras deverão ser inspecionadas manualmente antes de aprovação final. O resultado **não** afirma teste visual automático de todas as faces.

O teste usa monitor virtual Xorg conectado (evita uma limitação do RandR no Xvfb com LWJGL 2) e Mesa/software OpenGL. A execução do jogo é limitada a **480 segundos** após o display estar pronto, e o job a **30 minutos**. Se travar, CI falha e anexa logs de Minecraft, OpenGL, processos, Xorg e capturas parciais.

Ainda são necessários testes de compatibilidade com Windows, GPUs reais e OptiFine; o PR permanece como rascunho até a avaliação dos resultados.
