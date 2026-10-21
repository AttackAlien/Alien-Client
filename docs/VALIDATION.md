# Validação da versão 0.1.0

Executada em 21 de outubro de 2026, no Linux x86_64 do ambiente de nuvem, com Temurin 8u472, Gradle 2.14.1, ForgeGradle 2.1 e Forge 1.8.9-11.15.1.2318/stable_22.

## Compilação e regressões

`setupDecompWorkspace build` concluiu com sucesso, usando dependências reais do Forge/Minecraft, TLS verificado e recursos oficiais com hashes/tamanhos verificados. **46 testes JUnit passaram, sem falhas, erros ou testes ignorados.**

As sete classes de testes cobrem configuração global, perfis/importação/isolamento por servidor, HUD, temas, cosméticos/modelos e mundo/PNG. O bootstrap foi repetido com sucesso sem alterar as ferramentas válidas. O wrapper foi executado com Java 8 e verificou a distribuição Gradle.

`reobfJar` gerou `build/libs/alien-client-0.1.0.jar` (122.907 bytes), com classes Java 8 e referências reofuscadas. A inspeção do JAR confirmou a ausência de classes do Minecraft, bibliotecas incorporadas ou código do teste gráfico.

SHA-256 do mod:

```text
cf3f4cb9bef67ef4300a0fc8c4d78d21a6693361cdaae33303db614207a5be62
```

## Cliente gráfico real

Comando repetível:

```bash
env -u DISPLAY ALIEN_TOOLS_DIR=/workspace/.alien-tools ./scripts/smoke-client.sh
```

O teste iniciou Xvfb/OpenGL por software, abriu o Minecraft/Forge real e criou um mundo local descartável. Na repetição para revisão visual, **54 verificações passaram**, com **16 capturas do framebuffer**. Entre elas:

- Descoberta do mod pelo Forge e substituição opcional do menu inicial.
- Menu Alien, persistência da aparência, presets e controles reais dos editores.
- Atalho Right Shift registrado e evento de keybinding abrindo o menu dentro do mundo.
- Capturas adicionais do menu de módulos e do World Studio enquanto o mundo está carregado.
- HUD de três linhas de diagnóstico, escala/alinhamento e prévia em mundo carregado.
- Jogador renderizado com acessórios 3D e controles do Cosmetic Studio.
- Céu noturno visual efetivamente desenhado em quatro e em dois chunks; no modo de dois chunks, terreno e jogador continuam visíveis.
- Panorama PNG importado, decodificado e visivelmente diferente no framebuffer.
- Horário real do servidor continuando normalmente e chuva/trovões reais inalterados durante overrides visuais.
- Salvamento/carregamento de perfil, ausência de erro de persistência e retorno ao menu após desconexão normal.
- Estado OpenGL sem erro após renderização e fechamento normal do mundo.

Evidência gerada: `build/smoke/result.txt`, `build/smoke/screenshots/` e logs no diretório de jogo descartável `build/smoke-runtime/`. O teste gráfico usa **as classes atuais de desenvolvimento**, sem carregar um JAR antigo; não simula a API do Minecraft. A instalação do JAR reofuscado em um launcher externo não foi executada nesta máquina.

A validação encontrou e corrigiu um vazamento de estado de profundidade no caminho de céu para distâncias curtas. O resultado final confirma a preservação do terreno e a restauração do estado gráfico. Os processos de teste e a tela Xvfb foram encerrados.

Na primeira repetição para revisão visual, a saída automatizada travou: o teste fechava o canal antes de chamar o descarregamento do mundo, e o servidor integrado podia parar antes de executar sua tarefa final. O thread dump foi preservado em `build/visual-review/first-attempt-shutdown-hang/`. O código de teste passou a acionar o botão vanilla Save and Quit no início do tick do cliente, sem alterar o código do mod. A repetição seguinte concluiu as 54 verificações e encerrou o jogo normalmente. Os 46 testes JUnit também foram executados novamente, sem falhas, erros ou ignorados.

## Limites da evidência

O ambiente está sem dispositivo de áudio e usa skin padrão quando a consulta de perfil online não responde. Áudio, autenticação/multiplayer público, outro sistema operacional e hardware gráfico real não foram validados. O teste local não demonstra que todas as combinações de outros mods funcionam.

Não foram medidos comparativos vanilla/Forge/Alien, e OptiFine não foi instalado. O protocolo está em [BENCHMARKS.md](BENCHMARKS.md). Os números visíveis nas capturas são diagnósticos de uma sessão de software com FPS limitado, não resultados de benchmark.

O rascunho do ambiente recebeu `install_script`, `start_skill` e os destinos de rede necessários. Isso salva instruções reutilizáveis; não publica o ambiente nem comprova restauração em uma nova tarefa. Revise, salve e publique nas configurações do ambiente para ativar o snapshot.
