# Primeiro teste do Alien Client Unified — Forge 1.8.9

Este é o **primeiro JAR único experimental** que substitui a escolha entre Legit e Full. O repositório ainda conserva os projetos antigos para fins históricos e regressão; instale **somente o Unified**, nunca dois Alien Clients ao mesmo tempo.

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

**Somente pacotes de execuções verdes devem ser instalados.** O artefato é experimental e costuma exigir login no GitHub; ele não é um GitHub Release público permanente. Se não houver um workflow da `main`, use temporariamente a execução já aprovada do PR #5: [run 39809488208](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488208). O arquivo interno `libs/AlienClient-Unified-1.8.9.jar` foi gerado da mesma base funcional, antes das atualizações de documentação.

## Instalação no Windows

1. Feche o Minecraft e faça backup das pastas `config/alien-client` (Legit) e `config/alien-client-full` (Full).
2. Apague/mova da pasta `mods` qualquer JAR antigo com ID `alienclient`. **Deixe somente** `AlienClient-Unified-1.8.9.jar` no perfil Forge escolhido.
3. Inicie o Minecraft no perfil **Forge 1.8.9** com **Java 8**.
4. Abra um mundo local, pressione **Right Shift** e acesse **Combat Studio**, **World Studio** e **HUD Editor**.
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

O [run 39809488208](https://github.com/AttackAlien/Alien-Client/actions/runs/39809488208) compilou o JAR Unified reofuscado e executou **108 testes JUnit e 24 verificações do Combat Studio em Minecraft/Forge real**. Outros workflows verificaram as edições legadas de sky e interface. **Ainda não há aprovação para Windows, GPU real, OptiFine, servidores ou testes físicos de clique.**

Os rótulos Legit/Full descrevem apenas presets e não garantem conformidade com regras de servidores. Fake anti-aim, silent aim, pacotes modificados e bypass não estão implementados.

## Para os desenvolvedores

```bash
bash ./scripts/bootstrap.sh
./gradlew --no-daemon -Pedition=unified setupDecompWorkspace build
ALIEN_EDITION=unified bash ./scripts/ci-combat-smoke-xorg.sh
python3 scripts/package-unified.py
```

O smoke Xorg requer Linux com display virtual e dependências do workflow; não se destina à máquina Windows de jogadores. O script de empacotamento **falha** se os relatórios JUnit ou a marca `PASS N checks` do Minecraft estiverem ausentes, preservando a cadeia de validação.
