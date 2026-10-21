# Validação gráfica de integração

Em 21/10/2026, a repetição para revisão visual terminou com **54 verificações aprovadas** e
16 capturas do framebuffer real. O resultado está em
`build/smoke/result.txt`, e as imagens em `build/smoke/screenshots/`.

O teste executa Minecraft 1.8.9 com Forge 11.15.1.2318 e Java 8, usando Xvfb
e OpenGL por software. A janela tem 854 × 480 pixels, com interface escalada
para 427 × 240. Ele usa um mundo superplano descartável em
`build/smoke-runtime/`, sem acessar os mundos de `run/`.

```bash
ALIEN_TOOLS_DIR=/workspace/.alien-tools ./scripts/smoke-client.sh
```

O mod de teste em `src/smoke/` é separado do código distribuído. O teste
confirma que as classes atuais de `build/classes/main` foram carregadas,
excluindo o JAR do classpath para evitar validar um pacote antigo. O build e
a reobfuscação do JAR distribuído são verificados separadamente.

## Comportamentos verificados

- Descoberta do mod pelo Forge, inicialização e menu inicial opcional.
- Renderização dos menus Alien, aparência, desempenho, HUD, cosméticos,
  mundo, efeitos e câmera/filtro.
- Capturas do menu de módulos e do World Studio com o mundo já carregado.
- Controles visíveis dos estúdios dentro da área escalada de 427 × 240.
- Alteração real de aparência, perfil de desempenho, escala e alinhamento
  de HUD, acessórios e opções dos estúdios.
- Atalho padrão Right Shift e abertura do menu por um evento da associação
  de teclas registrada no Minecraft. O evento é gerado pelo teste, sem
  automação de teclado do sistema operacional.
- Entrada em um mundo integrado, HUD real e acessórios procedurais no
  jogador e na prévia 3D.
- Céu personalizado visivelmente diferente do céu diurno vanilla, tanto
  com distância de quatro chunks quanto no mínimo de dois chunks. No
  segundo caso, o terreno e o personagem continuam visíveis.
- Horário e clima reais do servidor integrado preservados ao selecionar
  meia-noite e neve visuais.
- Salvamento e carregamento de perfil no diretório real da instância.
- Importação de PNG, upload da textura e mudança visível no céu renderizado.
- Ausência de erro OpenGL após os estúdios e a renderização no mundo.
- Encerramento normal da conexão local e retorno ao menu Alien.

A primeira tentativa dessa repetição travou na saída automatizada do servidor
integrado. O teste foi corrigido para executar o botão Save and Quit da tela
vanilla no início de um tick do cliente. A repetição aprovada não altera o código
de produção do mod e encerra o mundo normalmente. Log e thread dump da tentativa
interrompida foram preservados em `build/visual-review/first-attempt-shutdown-hang/`.

O último log da execução aprovada não contém falhas fatais nem exceções de
pacotes durante a desconexão. A execução sem conta usa a skin padrão; os
avisos de consulta de skin, Twitch e áudio indisponível no ambiente não
impediram o teste.

## Limites desta evidência

Este é um teste funcional, com limite de 30 FPS e renderização por software;
os números exibidos no HUD **não são resultados de benchmark**. Não valida
ganho de FPS, compatibilidade com OptiFine, servidores externos, publicação
do ambiente ou instalação do JAR em um launcher do usuário. Também não
substitui testes manuais com GPUs, resoluções e outros mods diferentes.

As verificações de interface geram entradas e invocam controles reais;
não avaliam automaticamente a qualidade estética, a usabilidade completa
ou todas as combinações de efeitos. Importação de modelos, entradas
inválidas e limites de configuração têm testes unitários próprios.
