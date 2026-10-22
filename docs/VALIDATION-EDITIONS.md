# Validação Legit/Full — 0.3.0-alpha.1

Executada em 22 de outubro de 2026 no Linux x86_64, Temurin 8u472, Gradle 2.14.1, ForgeGradle 2.1, Forge 1.8.9-11.15.1.2318/stable_22 e Mesa llvmpipe. Esta rodada valida a primeira fase de arquitetura; [0.2.0](VALIDATION.md) é a base visual preservada.

| Verificação | Legit | Full |
| --- | --- | --- |
| `setupDecompWorkspace build` | Passou | Passou |
| Testes JUnit | 91; zero falhas/erros/ignorados | 91; zero falhas/erros/ignorados |
| Inspeção do JAR reofuscado | Passou; 82 classes | Passou; 82 classes |
| Teste gráfico Minecraft/Forge real | 100 verificações passaram | 100 verificações passaram |
| PNGs originais | 35 | 35 |
| Movimento da capa, 12 quadros | Até 98 pixels alterados no recorte do tecido | Até 95 pixels alterados no recorte do tecido |

[Capturas e resultados originais](../captures/editions/README.md) · [SHA-256 dos JARs](../downloads/v0.3.0-alpha.1/SHA256SUMS.txt).

## O que foi exercitado

Além das regressões visuais da 0.2.0, cada execução confirmou o nome/versão reais descobertos pelo Forge, o provider da edição selecionada, um único marcador de edição no classpath, a raiz de configuração correta e nove registros visuais compartilhados. O evento real de keybinding alternou o céu em um mundo carregado e restaurou seu estado; pressões com um editor aberto foram consumidas sem ativação posterior.

Ambas executaram menu, atalho Right Shift, controles dos editores, prévia 3D, capa animada, céus a 2/4 chunks, importação PNG, Crystal/Sakura com partículas/HUD, perfis e Save and Quit. O horário/clima reais do servidor foram preservados e OpenGL terminou sem erro. A Full carregou os panoramas da sua própria raiz `config/alien-client-full/`.

Os 91 testes de cada composição são a mesma suíte compartilhada, executada separadamente nos dois classpaths. Incluem os 14 testes do registro, que rejeitam módulos Full e impactos não visuais na Legit, verificam troca de perfil, metadados, consultas imutáveis e atalhos limitados/determinísticos. Não representam 182 módulos diferentes.

## Inspeção e testes de rejeição

O build verifica as classes Java 8 reais, a origem `SourceFile` em fontes compartilhadas/selecionadas, referências a tipos exclusivos Full, recursos, provider compilado, anotação Forge `@Mod`, manifesto, JSON e chamadas SRG reofuscadas. O pacote não inclui fontes Java, classes Minecraft/Forge nem o smoke.

Uma cópia temporária do JAR Legit recebeu o **provider realmente compilado da Full**. O verificador a rejeitou por divergência da identidade compilada; o original permaneceu intacto. Um artefato malformado também foi rejeitado. A seleção inválida de edição foi recusada antes da compilação e antes do smoke.

Esta prova cobre a fronteira atual, composta pelos providers e recursos específicos. Não demonstra a implementação de módulos avançados: **nenhum está registrado ou implementado nesta fase**. Novas classes Full deverão continuar sendo inspecionadas pelo mesmo limite de fontes e referências.

## Comandos realmente executados

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
./gradlew --no-daemon -Pedition=legit setupDecompWorkspace build
./gradlew --no-daemon -Pedition=full setupDecompWorkspace build
env -u DISPLAY ALIEN_EDITION=legit ./scripts/smoke-client.sh
env -u DISPLAY ALIEN_EDITION=full ./scripts/smoke-client.sh
```

Saídas em `build/<edition>/test-results/`, `reports/tests/`, `libs/`, `smoke/` e `smoke-runtime/`. Os JARs só foram copiados para downloads depois da aprovação de ambas as composições. `scripts/build-editions.sh` reúne esses passos para próximas rodadas; esta rodada executou os comandos individualmente.

## Limites e pendências

O jogo executou as classes atuais de desenvolvimento, não uma instalação dos JARs em launcher externo. A validação usou software OpenGL e limite de 30 FPS; não é benchmark. Não foram testados áudio, OptiFine, outros mods, GPUs reais, outros sistemas operacionais ou servidores privados/públicos. A configuração do ambiente foi salva em rascunho; isso não publica um snapshot nem prova restauração em outra tarefa.

A fase 1 não conclui o master plan: módulos avançados Full, demais módulos Legit, cubemaps/OptiFine Custom Sky, blur/shaders e o restante do acabamento premium permanecem classificados na [auditoria](ARCHITECTURE-AUDIT.md). AutoTotem/offhand modernos são incompatíveis com 1.8.9 vanilla. A capa não tem colisões físicas; partículas são formas planas; modelos externos continuam limitados a caixas JSON.
