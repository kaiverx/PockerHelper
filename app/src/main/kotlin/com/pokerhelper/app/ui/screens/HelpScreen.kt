package com.pokerhelper.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pokerhelper.app.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.help_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            CombinationsSection()
            HandComparisonSection()
            EvaluatorSection()
            EquitySection()
            PotOddsSection()
            ExpectedValueSection()
            OutsSection()
            VarianceSection()
            GlossarySection()
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CombinationsSection() {
    Section(
        title = stringResource(R.string.help_section_combinations),
        subtitle = stringResource(R.string.help_section_combinations_sub)
    ) {
        Item("Роял-флеш", "A-K-Q-J-T одной масти. Старший вариант стрит-флеша. Вероятность на префлопе: 0.000154%.")
        Item("Стрит-флеш", "5 подряд одной масти. Например: 9♠-8♠-7♠-6♠-5♠. Вероятность: 0.00139%.")
        Item("Каре", "4 карты одного ранга + кикер. Вероятность: 0.024%.")
        Item("Фулл-хаус", "Тройка + пара. Например: K-K-K-Q-Q. Вероятность: 0.144%.")
        Item("Флеш", "5 одной масти, не подряд. Вероятность: 0.197%.")
        Item("Стрит", "5 подряд разных мастей. Вероятность: 0.392%.")
        Item("Сет / тройка", "3 карты одного ранга. Сет — когда тройка из пары в кармане + 1 на борде. Трипс — наоборот.")
        Item("Две пары", "Две разные пары + кикер. Например: A-A-K-K-Q.")
        Item("Пара", "2 карты одного ранга + 3 кикера.")
        Item("Старшая карта", "Ничего из выше. Сравнивается по убыванию рангов.")
    }
}

@Composable
private fun HandComparisonSection() {
    Section(title = stringResource(R.string.help_section_comparison)) {
        Body(
            "Когда у двух игроков комбинация одной категории, побеждает рука со старшими " +
            "ключевыми картами. В коде каждая рука получает массив tiebreakers, по которым " +
            "сравнение идёт лексикографически (как слова в словаре)."
        )
        Mono("Пара:       [ранг пары, кикер1, кикер2, кикер3]")
        Mono("Две пары:   [старшая пара, младшая пара, кикер]")
        Mono("Сет:        [ранг тройки, кикер1, кикер2]")
        Mono("Стрит:      [старшая карта стрита]")
        Mono("Флеш:       [все 5 рангов по убыванию]")
        Mono("Фулл-хаус:  [ранг тройки, ранг пары]")
        Mono("Каре:       [ранг каре, кикер]")
        Body(
            "Пример: AAKK5 (две пары) vs QQJJ9 — сравниваем [14,13,5] vs [12,11,9]. " +
            "Первая выигрывает: 14 > 12 — старшая пара важнее всего остального."
        )
        Body(
            "Особый случай — «колесо» A-2-3-4-5. Туз тут младший, старшая карта стрита — " +
            "пятёрка. Это единственное место в покере, где туз младше двойки."
        )
    }
}

@Composable
private fun EvaluatorSection() {
    Section(title = stringResource(R.string.help_section_evaluator)) {
        Body(
            "Из 7 доступных карт (2 карманные + 5 борд) программа выбирает лучшую " +
            "5-карточную комбинацию. Перебираются все C(7,5) = 21 подмножество, " +
            "для каждого считаются категория и tiebreakers, выбирается максимум."
        )
        Body(
            "Эта реализация делает ~21 итерацию на одно вычисление. Для 5000 симуляций " +
            "× (1 наша + N оппонентов) — порядка 100-500 тысяч операций, что занимает " +
            "примерно секунду на телефоне."
        )
        Body(
            "Профессиональные эвалюаторы (Cactus Kev, 2+2) используют lookup-таблицы: " +
            "одна операция ~50 наносекунд вместо ~10 микросекунд. В 200 раз быстрее. " +
            "Если упрёмся в производительность — заменим без изменения интерфейса."
        )
    }
}

@Composable
private fun EquitySection() {
    Section(title = stringResource(R.string.help_section_equity)) {
        Body(
            "Equity — доля банка, которую рука в среднем заберёт на шоудауне с учётом " +
            "ещё не открытых карт и случайных рук оппонентов."
        )
        Body("Расчёт методом Монте-Карло:")
        Body("• Берём колоду без известных карт")
        Body("• Случайно раздаём оппонентам и доборные карты борда")
        Body("• Эвалюируем все руки, считаем кто победил")
        Body("• Повторяем 5000 раз")
        Mono("equity = wins/N + ties/(2·N)")
        Body(
            "Деление ties на 2 — упрощение для случая ничьей с одним оппонентом. " +
            "Если ничейная рука раскалывает банк с N игроками — точнее делить на N+1, " +
            "но многоплеерные ничьи редки, и погрешность невелика."
        )
        Body("Точность Monte Carlo по центральной предельной теореме:")
        Mono("σ ≈ √(p·(1−p)/N)")
        Body(
            "где p — истинное equity (макс. 0.5), N — число итераций. Для N=5000: σ ≈ 0.007, " +
            "то есть ±0.7%. Если истинное equity 42.3%, программа покажет 41.6%-43.0%."
        )
        Body(
            "Почему не точный расчёт. Аналитическое решение требует перебрать все возможные " +
            "раздачи карт. На префлопе против 1 оппонента это C(50,2) × C(48,5) ≈ 2.1 млрд " +
            "комбинаций. Monte Carlo делает компромисс: вместо «посчитать всё» — «посчитать " +
            "на случайной выборке»."
        )
    }
}

@Composable
private fun PotOddsSection() {
    Section(title = stringResource(R.string.help_section_pot_odds)) {
        Body(
            "Pot odds — минимальное equity, при котором колл прибылен в долгосрочной " +
            "перспективе. Если ваше equity больше pot odds — колл +EV, иначе −EV."
        )
        Mono("pot_odds = call / (pot + call)")
        Body(
            "Логика: если уравнять, итоговый банк = pot + call(оппонента) + call(наш). " +
            "Наша доля вложения в банк = call / (pot + call). Это и есть минимальное " +
            "нужное equity, чтобы выйти в ноль."
        )
        Body("Пример: банк 100, колл 50. Pot odds = 50/150 = 33.3%. Если equity 40% — колл +EV.")
        Body(
            "В литературе встречается альтернативная формула call / (pot + 2·call), которая " +
            "даёт более точное безубыточное equity (в примере 25% вместо 33%). Мы используем " +
            "более распространённую — она чуть консервативнее, что безопаснее на практике."
        )
    }
}

@Composable
private fun ExpectedValueSection() {
    Section(title = stringResource(R.string.help_section_ev)) {
        Body(
            "EV решения — среднее количество фишек, которое мы выиграем (или потеряем) в " +
            "долгосрочной перспективе при многократном повторении этой же ситуации."
        )
        Mono("EV(call) = p_win · pot_won − p_lose · call")
        Body("где p_win = equity, p_lose = 1 − equity, pot_won — банк, который заберём при победе.")
        Body(
            "Пример: банк 100, колл 50, equity 40%. " +
            "EV = 0.40 × 150 − 0.60 × 50 = 60 − 30 = +30. " +
            "В среднем каждое такое решение приносит 30 фишек прибыли."
        )
        Body(
            "Pot odds — частный случай EV: пороговое equity, при котором EV = 0. " +
            "Сравнение equity с pot odds = проверка знака EV."
        )
        Body(
            "Implied odds — то же, но с учётом потенциальных выигрышей на следующих улицах. " +
            "На дро-руке прямой EV колла может быть отрицательный, но если на ривере получится " +
            "выбить много фишек у оппонента — реальный EV положительный."
        )
    }
}

@Composable
private fun OutsSection() {
    Section(title = stringResource(R.string.help_section_outs)) {
        Body(
            "Аут — карта следующей улицы, которая делает нашу руку победной. В приложении " +
            "подсвечены оранжевым в сетке выбора."
        )
        Body(
            "Алгоритм: для каждой оставшейся в колоде карты симулируем 200 раздач с этой " +
            "картой на следующей улице. Если в достаточной доле симуляций мы выигрываем — " +
            "карта засчитывается как аут."
        )
        Body(
            "Порог адаптивный: при 1 оппоненте — 70% побед, при 9 оппонентах — 30%. " +
            "Логика: с 9 случайными противниками шанс что у кого-то лучшая рука выше, " +
            "и завышать порог нет смысла."
        )
        Body(
            "Подход отличается от классического («карты, улучшающие комбинацию») тем, что " +
            "учитывает грязные ауты (карта улучшает нас, но даёт оппоненту лучше) и бэкдоры."
        )
        Body("Считается только на флопе и тёрне. На префлопе слишком много неизвестных.")
        Body("Классическое правило 2/4 для быстрой оценки equity дро:")
        Mono("На тёрне:  equity ≈ outs · 2")
        Mono("На флопе:  equity ≈ outs · 4")
        Body(
            "Откуда: на тёрне 46 неизвестных карт, P = O/46 ≈ O × 2.17%. " +
            "На флопе формула учитывает 2 шанса получить аут: 1 − (1 − O/47)·(1 − O/46) ≈ O × 4%."
        )
    }
}

@Composable
private fun VarianceSection() {
    Section(title = stringResource(R.string.help_section_variance)) {
        Body(
            "Покер — игра с большой дисперсией. Даже играя оптимально, в коротких отрезках " +
            "можно проигрывать. Закон больших чисел работает только на длинных дистанциях."
        )
        Body(
            "Пример: рука с equity 70% выигрывает 7 из 10 раз в среднем. Конкретные 10 " +
            "рук могут дать любой результат от 0 до 10. Стандартное отклонение для 10 " +
            "раздач: σ = √(10·0.7·0.3) ≈ 1.45, то есть 95% результатов в диапазоне 4-10 побед."
        )
        Body(
            "Для 1000 раздач σ ≈ 14.5, диапазон 671-729 побед. Только на больших выборках " +
            "статистика становится надёжной."
        )
        Body(
            "Практическое следствие: оценивать качество своей игры по 50-100 рукам некорректно. " +
            "Нужны тысячи раздач или анализ конкретных решений."
        )
    }
}

// ============ СЛОВАРЬ ============

private data class GlossaryTerm(val term: String, val definition: String)

private val glossary = listOf(
    GlossaryTerm("Equity", "Доля банка, которую рука в среднем заберёт на шоудауне. Выражается в процентах."),
    GlossaryTerm("Pot Odds", "Соотношение размера колла к общему банку после колла. Минимальное equity для безубыточного колла."),
    GlossaryTerm("EV (Expected Value)", "Математическое ожидание решения в фишках. Положительный EV = решение прибыльно в долгосрочной перспективе."),
    GlossaryTerm("Implied Odds", "Pot odds с учётом потенциальных будущих выигрышей. Помогает оправдать колл со слабой рукой при большом стеке оппонента."),
    GlossaryTerm("Outs", "Карты, которые улучшат нашу руку до победной на следующих улицах."),
    GlossaryTerm("Префлоп", "Стадия раздачи до открытия общих карт. У каждого игрока только 2 карманные карты."),
    GlossaryTerm("Флоп", "Первые 3 общие карты, открываемые одновременно. Вторая стадия раздачи."),
    GlossaryTerm("Тёрн", "Четвёртая общая карта. Третья стадия раздачи."),
    GlossaryTerm("Ривер", "Пятая (последняя) общая карта. Финальная стадия."),
    GlossaryTerm("Шоудаун", "Вскрытие карт после ривера, когда определяется победитель."),
    GlossaryTerm("Карманные карты (hole cards)", "Две закрытые карты, выданные игроку. В холдеме видны только владельцу."),
    GlossaryTerm("Борд", "Общие карты на столе: флоп + тёрн + ривер. Используются всеми игроками."),
    GlossaryTerm("Кикер", "Карта, не входящая в комбинацию, но участвующая в сравнении. У AAK кикер — K."),
    GlossaryTerm("Дро (draw)", "Незавершённая рука, которая может стать сильной при правильной карте. Флеш-дро = 4 одной масти."),
    GlossaryTerm("Бэкдор", "Дро, для завершения которого нужно две правильные карты подряд (тёрн + ривер)."),
    GlossaryTerm("Грязный аут", "Карта, которая улучшает нашу руку, но даёт оппоненту ещё более сильную."),
    GlossaryTerm("Сет", "Тройка из пары в кармане + одной карты на борде. Сильнее визуально маскируется."),
    GlossaryTerm("Трипс", "Тройка из одной карты в кармане + пары на борде. Слабее сета — оппонент тоже может иметь трипс."),
    GlossaryTerm("Колесо (wheel)", "Стрит A-2-3-4-5. Единственный стрит, где туз играет как 1, а не как 14."),
    GlossaryTerm("Оверкарта", "Карта в руке выше любой карты на борде. AK на борде Q-7-3 — две оверкарты."),
    GlossaryTerm("Топ-пара", "Пара со старшей картой борда. На борде K-7-3 пара королей — топ-пара."),
    GlossaryTerm("EV+ / EV−", "Решение с положительным/отрицательным математическим ожиданием."),
    GlossaryTerm("Дисперсия", "Колебания результата вокруг ожидаемого. В покере высокая — долгосрочное преимущество видно только на большой выборке."),
    GlossaryTerm("ICM", "Independent Chip Model — модель оценки реальной стоимости фишек в турнире. Не применяется в кэш-играх."),
    GlossaryTerm("GTO", "Game Theory Optimal — теоретически невзламываемая стратегия равновесия Нэша. Сложна для запоминания, на практике используется как ориентир."),
    GlossaryTerm("Эксплуатативная игра", "Стратегия, отклоняющаяся от GTO для использования ошибок конкретного оппонента."),
    GlossaryTerm("Range (диапазон)", "Множество всех рук, с которыми игрок мог принять конкретное решение."),
    GlossaryTerm("Tiebreaker", "Дополнительные значения для разрешения ничьих внутри одной категории комбинаций.")
)

@Composable
private fun GlossarySection() {
    Section(
        title = stringResource(R.string.help_section_glossary),
        subtitle = stringResource(R.string.help_section_glossary_sub)
    ) {
        glossary.forEach { GlossaryItem(it) }
    }
}

@Composable
private fun GlossaryItem(item: GlossaryTerm) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
            .padding(vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowDown
                              else Icons.Default.KeyboardArrowRight,
                contentDescription = stringResource(
                    if (expanded) R.string.cd_collapse else R.string.cd_expand
                ),
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(4.dp))
            Text(
                item.term,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        if (expanded) {
            Text(
                item.definition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun Section(title: String, subtitle: String? = null, content: @Composable ColumnScope.() -> Unit) {
    Spacer(Modifier.height(20.dp))
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
    subtitle?.let {
        Text(
            it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    Spacer(Modifier.height(8.dp))
    Column(content = content)
}

@Composable
private fun Item(name: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            name,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            desc,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun Body(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

/** Моноширинный блок для формул. */
@Composable
private fun Mono(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Text(
            text,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
