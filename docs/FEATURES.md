# Escopo da versão 0.2.0

A tabela descreve a implementação disponível no código. Resultados de compilação e execução gráfica são registrados separadamente em [VALIDATION.md](VALIDATION.md); a presença de um controle não demonstra compatibilidade com outros mods ou ganho de FPS.

| Área | Implementado | Limites e extensões pendentes |
| --- | --- | --- |
| Base Forge | Mod apenas de cliente, inicialização, tecla configurável, configuração JSON atômica, backups de dados inválidos | Sem launcher próprio; requer Minecraft/Forge do usuário |
| Interface | Menu inicial opcional; layouts Panels e Studio; colunas flutuantes arrastáveis, recolhíveis e com rolagem; busca de módulos; gradientes, RGB, transparência, sliders, atalhos, animações e temas JSON | Fonte padrão do jogo; Studio pesquisa categorias e não cada opção dos subeditores; telas pequenas distribuem colunas em páginas |
| HUD | Doze tipos de elementos, incluindo marca Alien com FPS real e lista de visuais habilitados; arraste, escala, alinhamento, grade, prévia, texto e preset compacto | Um elemento de texto personalizado; a lista reflete configurações locais e não módulos de combate; não há editor de scripts/plugins de HUD |
| Cosméticos | Malhas originais trianguladas para kasa cônico, kabuto, coroa, kitsune, visor, óculos, asas, mochila e charm; capa curva animada com textura e emblemas; aura/trilha | A capa usa deformação procedural, sem simulação física de colisões; o importador externo continua limitado a caixas JSON; não altera skins |
| Cosmetic Studio | Prévia 3D com arraste para girar, roda para zoom, turntable e reset; abas Style/Fit/Players/Imports/Motion; seletor RGB/hex, presets Ronin/Celestial, ajuste de tamanho, posição, rotação, capa e detalhe | Requer jogador carregado para prévia; emblemas são os padrões locais Alien/Moon/Stars/Plain, sem editor arbitrário de desenhos |
| Inspector | Seleção de jogadores carregados e UUIDs já salvos, acessórios por jogador, reset individual/global | Sem editor arbitrário de skin; jogador offline não tem prévia |
| Importação | Modelos JSON de caixas, texturas PNG e panoramas equiretangulares PNG locais | Sem download por URL, pacotes ZIP, scripts ou formatos de animação externos |
| Mundo | Temas Galaxy/Aurora/Japanese/Nebula/Crystal/Sakura, panorama 360°, gradiente, estrelas, sol/lua, horário visual e neblina; caminho de céu para distâncias de 2/3 chunks | Apenas dimensão 0; discos de sol/lua, sem editor de suas texturas; não altera a iluminação real do terreno |
| Clima | Precipitação visual Server/Clear/Rain/Snow/Storm, intensidade e orçamento | Storm é chuva visual intensa; não cria trovões/raios/áudio; chuva real pode continuar audível no modo Clear |
| Efeitos | Movimento, aterrissagem, trilhas e partículas ambientes em formas Diamond/Petal/Spark; paletas integradas Crystal/Sakura/Nebula; animações de acessórios/interface, roll de câmera e filtro de cor | Partículas são malhas planas voltadas à câmera, sem shaders volumétricos; não há editor de animação específico de itens nem pipeline de pós-processamento |
| Desempenho | Limites de partículas/acessórios, distância, descarte de partículas atrás da câmera, caches limitados de malhas/texturas/estrelas, grade de capa reutilizada, detalhe Low/Balanced/High, FPS/frame time/P95/memória | O detalhe altera a subdivisão da capa, sem sistema geral de LOD para todos os modelos; limites são por subsistema e não abrangem partículas vanilla; sem promessas ou resultados comparativos de FPS |
| Perfis | Persistência, import/export, padrões, associação por servidor e restauração global | Associação local; nenhuma configuração enviada ao servidor |

## Uso dos novos controles

Right Shift abre o layout configurado. **Panels** é o padrão e reúne HUD, Cosmetics, World, Effects e Client em colunas. Arraste o cabeçalho para reposicionar, use o controle da extremidade direita para recolher e o ícone de editor ao lado ou o botão direito para abrir o editor. A roda percorre as opções. As posições e o estado recolhido são salvos no perfil. **Reset** restaura o layout das colunas, sem restaurar todas as configurações do cliente. Em **Settings → Appearance → Module menu**, escolha **Studio** para o painel com navegação lateral.

No HUD Editor, **Compact preset** deixa marca/FPS, lista de visuais habilitados e coordenadas. Os demais elementos continuam disponíveis para ativação e edição. Perfis anteriores mantêm suas posições; os dois elementos novos podem ser ativados individualmente no editor ou pelo preset.

No Cosmetic Studio, **Style** escolhe acessórios e cores; **Fit** ajusta transformações; **Players** controla overrides por UUID; **Imports** mantém o formato externo de caixas; **Motion** controla animação, ondulação/comprimento da capa, tamanho do chapéu, detalhe e emblema. O desenho do emblema acompanha as coordenadas da malha deformada. A opção global de animações e a opção do acessório podem interromper o movimento.

**World Studio → Atmosphere presets** ou **Effects Studio → Presets / shapes** abre as paletas. **Crystal** combina céu ciano e partículas em losango; **Sakura**, entardecer rosado e pétalas; **Nebula**, noite violeta e brilhos. Aplicar uma paleta ativa céu e efeitos, configura horário visual, neblina, clima visual Clear e cores, e limpa o nome do panorama importado. A aba **Particles** permite alternar forma, partículas ambientes e o interruptor geral. Os ajustes finos de quantidade, tamanho, distância e qualidade permanecem no Effects Studio.

## Compatibilidade com OptiFine

O mod usa as interfaces públicas de Forge 1.8.9 (`LayerRenderer`, `IRenderHandler`, eventos de HUD/câmera/neblina). Não inclui transformadores de bytecode ou arquivos OptiFine. Isso reduz acoplamento, mas não demonstra compatibilidade em execução.

A renderização de céu/clima pode competir com custom skies, shaders ou outros mods que substituem os mesmos handlers. Quando desativados, os handlers anteriores são restaurados. Testar com OptiFine 1.8.9 é opcional e exige uma cópia obtida pelo usuário nos canais autorizados. Essa combinação ainda não foi validada; não redistribuímos OptiFine. Procedimento em [BENCHMARKS.md](BENCHMARKS.md).

## Compartilhamento futuro e opcional

O formato atual já identifica preferências por UUID. Uma extensão poderá compartilhar somente identificadores de modelos aprovados, cores e transformações, com consentimento explícito e opção de desligar. O catálogo deve validar tamanho, hash e licença de cada recurso; recursos desconhecidos usam fallback local. Uma camada de transporte separada deverá ter limites de requisição/cache e não tocar atributos, inventário, skins de terceiros ou regras do servidor. Nenhum serviço de compartilhamento ou tráfego desse tipo está implementado nesta versão.
