# Medição de desempenho e compatibilidade

Não há resultado comparativo de FPS nesta entrega. O ambiente de nuvem usa hardware/renderização diferente de uma instalação de jogo; iniciar o cliente nele demonstra carregamento e renderização, não ganho de desempenho.

## Protocolo reproduzível

1. Prepare três instalações independentes: Minecraft 1.8.9 vanilla; Forge 1.8.9-11.15.1.2318 sem Alien; o mesmo Forge com Alien. Use o mesmo JDK 8, heap, driver e resolução. Não adicione OptiFine ao comparativo base.
2. Copie o mesmo mundo local com a mesma seed/estado. Fixe posição e orientação do jogador, distância de renderização, VSync, limite de FPS, gráficos, recursos, partículas e opções de animação. Registre CPU, GPU, memória e versões.
3. Aqueça cada instalação por pelo menos dois minutos; faça três passagens de 60 segundos por cenário. Alterne a ordem das instalações para reduzir efeitos térmicos/cache. Não gere chunks novos durante a amostra.
4. Use o mesmo capturador externo de frame time nas três instalações. Reporte FPS mediano, percentis 95/99 de frame time e memória, com dispersão entre repetições. A janela curta do HUD Alien é apenas diagnóstica e não serve como instrumento comum às três instalações.
5. Compare Alien com efeitos desligados e depois os presets Competitive, Balanced e Cinematic. Registre também quantidade de jogadores, acessórios ativos, céu e clima. Não atribua às otimizações a redução de trabalho causada por desligar efeitos adicionados pelo próprio mod.

Tabela para preencher com dados medidos:

| Cenário | Vanilla | Forge | Alien Competitive | Alien Balanced | Alien Cinematic |
| --- | --- | --- | --- | --- | --- |
| FPS mediano / 3 passagens | não medido | não medido | não medido | não medido | não medido |
| Frame time P95/P99 | não medido | não medido | não medido | não medido | não medido |
| Memória após aquecimento | não medido | não medido | não medido | não medido | não medido |

## OptiFine: verificação separada

Obtenha legalmente uma versão compatível com Forge 1.8.9; não a adicione ao repositório nem aos artefatos. Repita os testes com shaders/custom sky inicialmente desligados. Verifique: entrada/saída de mundo, F5, modelo slim/default, transparências, troca de céu/clima e restauração dos handlers, água/lava/cegueira, HUD/F3, redimensionamento e recarregamento de recursos.

Depois teste shaders/custom sky um de cada vez. Se outro mod controlar o mesmo céu, mantenha o céu Alien desligado durante o diagnóstico. Guarde versões e logs sem tokens/dados de conta. Compatibilidade só deve ser declarada após esses testes reais.
