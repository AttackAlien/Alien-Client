# Primeiro teste do Alien Client Unified — Forge 1.8.9

O Unified é o **JAR único experimental** que substitui a escolha entre Legit e Full. Esta branch integra o redesign visual 0.4 e os recursos dos PRs #3–#7. Isso ainda não é uma release estável nem um merge na `main`. O repositório conserva as edições antigas para regressão; instale **somente o Unified**, nunca dois Alien Clients ao mesmo tempo.

## Requisitos

- Minecraft **Java Edition 1.8.9**.
- **Minecraft Forge 11.15.1.2318** para 1.8.9.
- **Java 8** configurado no launcher (evite Java 17/21 para Forge 1.8.9).
- Windows: pasta `%APPDATA%\.minecraft\mods` no launcher padrão; launchers com diretórios isolados podem usar outra pasta.

## Download verificado

O GitHub Actions [Alien Client Unified | Forge 1.8.9](../.github/workflows/unified-client-ci.yml) produz um único artefato `alien-unified-<run_id>` depois de **compilação Java 8, testes JUnit, inspeção do JAR e smoke em Minecraft real (Xorg/Mesa)**. Dentro do artefato:

- `distribution/AlienClient-Unified-Primeiro-Teste.zip` — pacote com o JAR, SHA256 e instruções em português.
- `distribution/AlienClient-Unified-1.8.9.jar` — instalador direto (é um mod Forge, não um arquivo executável).
- `distribution/SHA256SUMS.txt` — hash SHA-256 do JAR.
- `combat-smoke/result.txt` e `combat-smoke/screenshots/` — comprovação de inicialização do jogo e da tela Combat Studio.
- `validation-inputs.json` — SHA256 do JAR, classes/recursos, fontes/build e relatórios JUnit capturados antes de executar o jogo.

**Somente pacotes de execuções verdes devem ser instalados.** Use a execução correspondente ao commit da integração que está sendo avaliado. O artefato é experimental e costuma exigir login no GitHub; ele não é um GitHub Release público permanente. Os JARs Legit/Full publicados anteriormente em `downloads/v0.4.0-alpha.1/` são previews históricos do PR #8 e não comprovam esta integração Unified.

## Instalação no Windows

1. Feche o Minecraft e faça backup das pastas `config/alien-client` (Legit) e `config/alien-client-full` (Full).
2. Apague/mova da pasta `mods` qualquer JAR antigo com ID `alienclient`. **Deixe somente** `AlienClient-Unified-1.8.9.jar` no perfil Forge escolhido.
3. Inicie o Minecraft no perfil **Forge 1.8.9** com **Java 8**.
4. Abra um mundo local, pressione **Right Shift** e acesse **Combat Studio**, **World Studio** e **HUD Editor**.
   No World Studio, confira também **Sky Gallery / Sky Options**; no Cosmetic Studio, **Looks** salva três conjuntos por perfil.
5. Faça os primeiros testes com **Combat OFF**. Em seguida, teste os presets Legit / Full / Custom somente num mundo local ou ambiente onde sejam permitidos.
6. Feche e abra o jogo novamente para confirmar que o perfil e o HUD permanecem salvos.

O Unified usa `config/alien-client/`; a pasta da antiga Full não é migrada automaticamente. Consulte [configurações de combate](UNIFIED-CLIENT.md).

## Roteiro de validação manual (não executado no CI)

- [ ] Forge 1.8.9 abre com o JAR, sem fechamento súbito nem erro de dependência
- [ ] O menu abre com Right Shift nas resoluções disponíveis
- [ ] Combat Studio mostra os perfis e salva mudanças entre sessões
- [ ] ClickGUI, HUD, Cosmetic Studio e World Studio continuam abrindo
- [ ] Céus panorâmicos e cubemaps são renderizados corretamente ao olhar nas seis direções
- [ ] Nenhuma textura fica invertida ou com costuras visíveis
- [ ] Testar em janela, tela cheia e diferentes resoluções
- [ ] Repetir os testes com OptiFine, **se for utilizado**; anotar versão
- [ ] Verificar FPS e travamentos durante ao menos uma sessão curta de jogo
- [ ] Não usar em servidor com políticas que proíbem assistência de combate

Para diagnosticar um problema no Windows, informe a versão do Java, Forge, GPU, lista de mods e anexe somente o `latest.log` e crash report relevantes, após remover dados pessoais.

## O que foi comprovado automaticamente

Como referência histórica, o [run 39809488208](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488208) compilou o Unified do PR #5 com **108 testes JUnit e 24 verificações do Combat Studio em Minecraft/Forge real**. Esses números não validam o redesign integrado; os resultados atuais ficam no relatório de integração e nos artefatos do commit correspondente.

O job Windows do PR #6 verifica somente extração do ZIP, presença do JAR e SHA256. **Ainda não há validação de Minecraft no Windows, GPU real, OptiFine, servidores ou testes físicos de clique.**

Os rótulos Legit/Full descrevem apenas presets e não garantem conformidade com regras de servidores. Fake anti-aim, silent aim, pacotes modificados e bypass não estão implementados.

## Para os desenvolvedores

```bash
bash ./scripts/bootstrap.sh
./gradlew --no-daemon -Pedition=unified clean setupDecompWorkspace build
python3 scripts/package-unified.py --begin-validation
ALIEN_EDITION=unified bash ./scripts/ci-combat-smoke-xorg.sh
python3 -m unittest discover -s scripts/tests -p 'test_package_unified.py' -v
python3 scripts/package-unified.py
```

O smoke Xorg requer Linux com display virtual e dependências do workflow; não se destina à máquina Windows de jogadores. Execute os comandos em sequência. `--begin-validation` congela os hashes dos inputs compilados/testados e apaga um PASS anterior. O empacotamento **falha** se JAR, fontes, classes, recursos ou relatórios JUnit mudarem, se o PASS for anterior à janela atual, ou se JUnit/Combat Studio não passarem. Uma recompilação exige nova janela e novo teste gráfico.

O jogo de desenvolvimento usa classes desofuscadas; o empacotador vincula esses inputs ao JAR reofuscado produzido e inspecionado pelo build. Isso não substitui a instalação manual do JAR no launcher de um jogador. Os onze testes Python usam cópias descartáveis dos artefatos reais, incluindo rejeição de JAR trocado, fontes/classes alteradas, resultados antigos e janela ausente.
