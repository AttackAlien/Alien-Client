> A versão atual 0.4.0-alpha.1 aplica o [redesign da interface](UI-OVERHAUL.md) sobre esta separação. Os comandos e namespaces abaixo continuam válidos. A evidência da primeira fase permanece no tag v0.3.0-alpha.1; a galeria atual contém apenas o redesign.

# Legit e Full — arquitetura 0.3.0-alpha.1

Esta revisão entrega a **primeira fase** do [master plan](MASTER-PLAN.md): auditoria e separação de composições. A [auditoria da base 0.2.0](ARCHITECTURE-AUDIT.md) preserva o estado anterior e classifica as lacunas. A Full é uma **prévia de arquitetura**: ainda não implementa Aim Assist, AutoClicker, Reach, Velocity, Flight, Speed, ESP ou automações de inventário.

## Fronteira de compilação

| Edição | Fontes compiladas | Configuração | JAR |
| --- | --- | --- | --- |
| Legit | `src/main` + `src/legit` | `config/alien-client/` | `AlienClient-Legit-1.8.9.jar` |
| Full | `src/main` + `src/full` | `config/alien-client-full/` | `AlienClient-Full-1.8.9.jar` |

O provider `BuildEdition` existe separadamente em cada raiz. Não há seletor runtime de edição. `-Pedition` aceita apenas `legit` ou `full`, com padrão `legit`. As saídas ficam em `build/legit/` e `build/full/`, incluindo classes, JARs, relatórios, recursos e mundos de teste. O código Full não entra no source path ou classpath de compilação Legit.

Os dois JARs usam o mesmo ID Forge `alienclient`; instale **um por vez** na instalação do Minecraft. São composições independentes do mesmo mod, não dois mods para carregar simultaneamente. A Legit mantém os arquivos da versão 0.2.0; a Full usa diretório próprio. Modelos, panoramas, perfis e temas seguem a raiz da respectiva edição.

## Registro e atalhos reais

`ModuleManager` registra nove interruptores existentes: HUD, cosméticos, céu, horário visual, neblina, precipitação, efeitos, camera sway e filtro de cor. Cada descritor possui ID, nome, categoria, descrição, nível de acesso e impacto. A GUI e os atalhos operam as configurações atuais; trocar de perfil não deixa os módulos presos ao objeto anterior.

O registro Legit rejeita descritores Full e impactos que alteram jogabilidade ou revelam informação oculta, mesmo se classificados indevidamente como compartilhados. Ambas as composições registram somente os nove interruptores visuais nesta fase. Não existem botões fictícios para os módulos avançados planejados.

Em **Options → Controls → Alien Modules**, defina os atalhos individuais. Começam sem tecla. `moduleKeys` armazena bindings por perfil, com limites de tamanho e códigos válidos. Pressões durante uma tela aberta são consumidas sem ativar módulos ou enfileirar ações para depois. Right Shift continua abrindo o menu Alien.

Esses nove descritores representam interruptores principais, não todo o catálogo de subopções. Os motores especializados e os buses públicos Forge/FML foram preservados. Um Render Engine genérico, editor próprio de keybinds/RGBA e o restante do acabamento premium permanecem no roteiro.

## Compilar e executar

Requer as mesmas ferramentas verificadas da base: Java 8, Gradle 2.14.1, ForgeGradle 2.1 e Forge 1.8.9-11.15.1.2318. Python 3.9+ inspeciona os artefatos.

```bash
export ALIEN_TOOLS_DIR=/workspace/.alien-tools
./gradlew --no-daemon -Pedition=legit build
./gradlew --no-daemon -Pedition=full build
ALIEN_EDITION=legit ./scripts/smoke-client.sh
ALIEN_EDITION=full ./scripts/smoke-client.sh
```

`./scripts/build-editions.sh` executa os dois builds e testes gráficos sequencialmente, verifica os binários e só então copia os dois JARs para `build/releases/`. O verificador inspeciona manifesto, metadados de edição e classes compiladas; testes gráficos não são incluídos no mod.

## Próximas fases

1. Evoluir componentes visuais e módulos Legit ausentes, sem refazer os motores já testados.
2. Acrescentar cubemaps/importadores e recursos próprios de céu, cosméticos e texturas.
3. Criar política de aviso/consentimento e ativação Full antes de implementar qualquer módulo que altere ações ou revele informação oculta; perfis/login multiplayer nunca devem ativá-los automaticamente.
4. Implementar módulos Full progressivamente, com testes reais locais; servidores privados exigem um destino autorizado disponível.
5. Medir desempenho e avaliar OptiFine/outros mods; publicar apenas afirmações apoiadas por resultados.

Nenhuma promessa de aceitação por servidores, compatibilidade universal ou evasão de anticheat. Os itens não implementados e os incompatíveis com Minecraft 1.8.9 vanilla estão na auditoria.

## Resultado da primeira fase

As duas composições passaram em **91 testes JUnit e 100 verificações gráficas cada**, com 35 PNGs por edição. Os JARs foram reofuscados e inspecionados; a troca indevida do provider Full no JAR Legit foi rejeitada pelo verificador. [Relatório completo](VALIDATION-EDITIONS.md) · [Imagens reais](https://github.com/AttackAlien/Alien-Client/blob/v0.3.0-alpha.1/captures/editions/README.md) · [Binários](../downloads/v0.3.0-alpha.1/README.md).
