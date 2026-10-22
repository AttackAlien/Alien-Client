# Alien Client 0.2.0 — Forge 1.8.9

Pré-release de personalização visual para **Minecraft Java 1.8.9 / Forge 11.15.1.2318 / Java 8**.

## Download e instalação

**[BAIXAR ALIEN CLIENT 0.2.0.jar](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/downloads/alien-client-0.2.0.jar?raw=true)**

[Capturas reais e capa em movimento](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/captures/README.md) · [SHA-256](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/downloads/SHA256SUMS.txt) · [Código-fonte ZIP](https://github.com/AttackAlien/Alien-Client/archive/refs/tags/v0.2.0.zip)

Instale o Forge indicado, use Java 8 e coloque somente o novo JAR em `mods`, substituindo a versão anterior. Dentro do mundo, **Right Shift** abre os painéis Alien.

O JAR compilado está versionado em `downloads`. O link acima baixa esse arquivo verificado do repositório; não há anexo nativo em Assets, pois o serviço de uploads desta integração recusou a autenticação.

## Mudanças

- Chapéus circulares e curvos, máscara com aberturas reais, asas de penas e mochila arredondada.
- Capa de tecido deformável, bordas curvas e emblemas originais Alien, Moon e Stars. Ajustes de ondulação, comprimento e detalhe.
- Cosmetic Studio redesenhado com prévia 360°, zoom e controles de cor, ajuste e movimento.
- Painéis pretos/roxos arrastáveis com busca, rolagem e posições persistidas; menu Studio continua disponível.
- HUD compacto branco com FPS real e lista de recursos visuais ativos.
- Crystal, Sakura e Nebula combinam céu, neblina e partículas de diamantes, pétalas ou faíscas.
- Perfis antigos mantêm seus layouts; configurações novas recebem padrões válidos.

## Validação

76 testes JUnit passaram sem falhas, erros ou ignorados. A execução gráfica e suas capturas estão documentadas em [VALIDATION.md](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/docs/VALIDATION.md).

## Limitações

Personalização local: não há compartilhamento de cosméticos pela rede. A capa usa ondas geométricas, sem colisões físicas; partículas são formas planas voltadas para a câmera. Importações de modelos continuam em caixas JSON limitadas; OBJ/glTF/Blockbench, shaders e importação por URL/ZIP não estão implementados.

Céu/clima personalizado restrito ao Overworld; horário, iluminação e clima reais do servidor continuam normais. Sem validação em launcher externo, multiplayer público, OptiFine, outros sistemas operacionais, áudio ou GPUs reais. Sem benchmark comparativo de FPS. Veja o [escopo completo](https://github.com/AttackAlien/Alien-Client/blob/v0.2.0/docs/FEATURES.md).
