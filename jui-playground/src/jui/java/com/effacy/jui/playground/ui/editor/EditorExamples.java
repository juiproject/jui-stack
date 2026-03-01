/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.playground.ui.editor;

import java.util.ArrayList;
import java.util.List;

import com.effacy.jui.core.client.control.IControl.Value;
import com.effacy.jui.core.client.dom.builder.Em;
import com.effacy.jui.core.client.dom.css.Length;
import com.effacy.jui.text.type.FormattedLine.FormatType;
import com.effacy.jui.text.type.markdown.MarkdownParser;
import com.effacy.jui.text.ui.editor2.Editor;
import com.effacy.jui.text.ui.editor2.EditorToolbar;
import com.effacy.jui.text.ui.editor2.FormattedTextEditor;
import com.effacy.jui.text.ui.editor2.IEditorToolbar.Position;
import com.effacy.jui.text.ui.editor2.LinkPanel;
import com.effacy.jui.text.ui.editor2.Tools;
import com.effacy.jui.text.ui.editor2.VariablePanel;
import com.effacy.jui.ui.client.icon.FontAwesome;
import com.effacy.jui.ui.client.panel.Panel;

/**
 * RendererExamples
 *
 * @author Jeremy Buckley
 */
public class EditorExamples extends Panel {private static final String SAMPLE_MARKDOWN0 = """
# World War II

After the Battle of France resulted in the French Third Republic capitulating to Nazi Germany in July 1940...

|Type|Grade|
|----|-----|
|Apple|Prime|

Robert's regime was overthrown by a local uprising in June of that year, which Fanon would later acclaim as...
        """;

    private static final String SAMPLE_MARKDOWN1 = """
# World War II

After the Battle of France resulted in the French Third Republic capitulating to Nazi Germany in July 1940, Martinique came under the control of French Navy elements led by Admiral Georges Robert who were loyal to the collaborationist Vichy regime. The disruption of imports from Metropolitan France led to major shortages on the island, which were exacerbated by an American naval blockade imposed on Martinique in April 1943. Robert's authoritarian regime repressed local Allied sympathizers, hundreds of whom escaped to nearby Caribbean islands. Fanon later described the Vichy regime in Martinique as taking off their masks and behaving like "authentic racists".[20] In January 1943, he fled Martinique during the wedding of one of his brothers and travelled to the British colony of Dominica in order to link up with other Allied sympathizers.[21]: 24 

Robert's regime was overthrown by a local uprising in June of that year, which Fanon would later acclaim as "the birth of the [Martinican] proletariat" as a revolutionary force. After the uprising, Fanon "enthusiastically" returned to Martinique, where Free French leader Charles de Gaulle had appointed Henri Tourtet as the colony's new governor. Tourtet subsequently raised the 5th Antillean Marching Battalion to serve in Free French Forces (FFL), and Fanon soon joined the unit in Fort-de-France.[22][23] He underwent basic training before boarding a troopship bound for Casablanca, Morocco in March 1944. After Fanon arrived in Morocco, he was shocked to discover the extent of racial discrimination in the FFL. He was subsequently transferred to a Free French military base in Béjaïa, Algeria, where Fanon witnessed firsthand the antisemitism and Islamophobia of the pieds-noirs, many of whom had supported racist laws promulgated by the Vichy regime.[24]

In August 1944, he departed on another troopship from Oran to France as part of Operation Dragoon, the Allied invasion of German-occupied Provence. After the US VI Corps secured a beachhead, Fanon's unit came ashore at Saint-Tropez and advanced inland. He participated in several engagements near Montbéliard, Doubs and was seriously wounded by shrapnel, which resulted in him being hospitalized for two months. Fanon was awarded a Croix de Guerre by Colonel Raoul Salan for his actions in battle, and in early 1945 rejoined his unit and fought in the Battle of Alsace.[25] After German forces had been pushed out of France and Allied troops crossed the Rhine into Germany, Fanon and his fellow black troops were removed from their formations and sent southwards to Toulon as part of de Gaulle's policy of removing non-white soldiers from the French army.[12] He was subsequently transferred to Normandy to await repatriation.[26]

Although Fanon had been initially eager to participate in the Allied war effort, the racism he witnessed during the war disillusioned him. Fanon wrote to his brother Joby from Europe that "I've been deceived, and I am paying for my mistakes... I'm sick of it all."[16] In the fall of 1945, a newly-discharged Fanon returned to Martinique, where he focused on completing his secondary education. Césaire, by now a friend and mentor of his, ran on the French Communist Party ticket as a delegate from Martinique to the first National Assembly of the French Fourth Republic, and Fanon worked for his campaign. Staying in Martinique long enough to complete his baccalauréat, Fanon proceeded to return to France, where he intended to study medicine and psychiatry.[citation needed]

# France

Fanon was educated at the University of Lyon, where he also studied literature, drama and philosophy, sometimes attending Merleau-Ponty's lectures. During this period, he wrote three plays, of which two survive.[27] After qualifying as a psychiatrist in 1951, Fanon did a residency in psychiatry at Saint-Alban-sur-Limagnole under the radical Catalan psychiatrist François Tosquelles, who invigorated Fanon's thinking by emphasizing the role of culture in psychopathology.

In 1948, Fanon started a relationship with Michèle Weyer, a medical student, who soon became pregnant. He left her for an 18-year-old high school student, Josie, whom he married in 1952. At the urging of his friends, he later recognized his daughter, Mireille, although he did not have contact with her.[28] Paulin Joachim, who knew Fanon, said that on a number of occasions he had seen Fanon hit Josie.[29]

In France, while completing his residency, Fanon wrote and published his first book, Black Skin, White Masks (1952), an analysis of the negative psychological effects of colonial subjugation upon black people. Originally, the manuscript was the doctoral dissertation, submitted at Lyon, entitled Essay on the Disalienation of the Black, which was a response to the racism that Fanon experienced while studying psychiatry and medicine at the University in Lyon; the rejection of the dissertation prompted Fanon to publish it as a book. In 1951, for his doctor of medicine degree, he submitted another dissertation of narrower scope and a different subject (Altérations mentales, modifications caractérielles, troubles psychiques et déficit intellectuel dans l'hérédo-dégénération spino-cérébelleuse : à propos d'un cas de maladie de Friedreich avec délire de possession – Mental alterations, character modifications, psychic disorders, and intellectual deficit in hereditary spinocerebellar degeneration: A case of Friedreich's disease with delusions of possession). Left-wing philosopher Francis Jeanson, leader of the pro-Algerian independence Jeanson network, read Fanon's manuscript and, as a senior book editor at Éditions du Seuil in Paris, gave the book its new title and wrote its epilogue.[30]

After receiving Fanon's manuscript at Seuil, Jeanson invited him to an editorial meeting. Amid Jeanson's praise of the book, Fanon exclaimed: "Not bad for a nigger, is it?" Insulted, Jeanson dismissed Fanon from his office. Later, Jeanson learned that his response had earned him the writer's lifelong respect, and Fanon acceded to Jeanson's suggestion that the book be entitled Black Skin, White Masks.[30]

In the book, Fanon described the unfair treatment of black people in France and how they were disapproved of by white people. Frantz argued that racism and dehumanization directed toward black people caused feelings of inferiority among black people. This dehumanization prevented black people from fully assimilating into white society and, further, into full personhood. This caused psychological strife among black people, as even if they spoke French, obtained an education, and followed social customs associated with white people, they would still never be regarded as French, or a Man; instead, black people are defined as "Black Man" rather than "Man". (See further discussion of Black Skin, White Masks under Work, below.)

# Algeria

After his residency, Fanon practised psychiatry at Pontorson, near Mont Saint-Michel, for another year and then (from 1953) in Algeria. He was chef de service at the Blida-Joinville Psychiatric Hospital in Algeria. He worked there until his deportation in January 1957.[31]

Fanon's methods of treatment started evolving, particularly by beginning socio-therapy to connect with his patients' cultural backgrounds. He also trained nurses and interns. Following the outbreak of the Algerian revolution in November 1954, Fanon joined the Front de Libération Nationale (FLN), after having made contact with Pierre Chaulet at Blida in 1955. Working at a French hospital in Algeria, Fanon became responsible for treating the psychological distress of the French soldiers and officers who carried out torture in order to suppress anti-colonial resistance. Additionally, Fanon was also responsible for treating Algerian torture victims.

Fanon made extensive trips across Algeria, mainly in the Kabylia region, to study the cultural and psychological life of Algerians. His lost study of "The marabout of Si Slimane" is an example. These trips were also a means for clandestine activities, notably in his visits to the ski resort of Chrea which hid an FLN base.

# Joining the FLN and exile from Algeria

By summer 1956, Fanon realized that he could no longer continue to support French efforts, even indirectly, via his hospital work. In November, he submitted his "Letter of Resignation to the Resident Minister", which later became an influential text of its own in anti-colonialist circles.[32]

There comes a time when silence becomes dishonesty. The ruling intentions of personal existence are not in accord with the permanent assaults on the most commonplace values. For many months, my conscience has been the seat of unpardonable debates. And the conclusion is the determination not to despair of man, in other words, of myself. The decision I have reached is that I cannot continue to bear a responsibility at no matter what cost, on the false pretext that there is nothing else to be done.

Shortly afterwards, Fanon was expelled from Algeria and moved to Tunis, where he joined the FLN openly. He was part of the editorial collective of Al Moudjahid, for which he wrote until the end of his life. He also served as Ambassador to Ghana for the Provisional Algerian Government (GPRA). He attended conferences in Accra, Conakry, Addis Ababa, Leopoldville, Cairo and Tripoli. Many of his shorter writings from this period were collected posthumously in the book Toward the African Revolution. In this book, Fanon reveals war tactical strategies; in one chapter, he discusses how to open a southern front to the war and how to run the supply lines.[31]

Upon his return to Tunis, after his exhausting trip across the Sahara to open a Third Front, Fanon was diagnosed with leukemia. He went to the Soviet Union for treatment and experienced remission of his illness. When he came back to Tunis once again, he dictated his testament The Wretched of the Earth. When he was not confined to his bed, he delivered lectures to Armée de Libération Nationale (ALN) officers at Ghardimao on the Algerian–Tunisian border. He traveled to Rome for a three-day meeting with Jean-Paul Sartre, who had greatly influenced his work. Sartre agreed to write a preface to Fanon's last book, The Wretched of the Earth.[33]

# Death and aftermath

With his health declining, Fanon's comrades urged him to seek treatment in the U.S. as his Soviet doctors had suggested.[34] In 1961, the CIA arranged a trip under the promise of stealth for further leukemia treatment at a National Institutes of Health facility.[34][35] During his time in the United States, Fanon was handled by CIA agent Oliver Iselin.[36] As Lewis R. Gordon points out, the circumstances of Fanon's stay are somewhat disputed: "What has become orthodoxy, however, is that he was kept in a hotel without treatment for several days until he contracted pneumonia."[34]

On 6 December 1961, Fanon died from double pneumonia at the National Institutes of Health Clinical Center in Bethesda, Maryland.[37] He had begun leukemia treatment but far too late.[38] He had been admitted under the name of Ibrahim Omar Fanon, a Libyan nom de guerre he had assumed in order to enter a hospital in Rome after being wounded in Morocco during a mission for the Algerian National Liberation Front.[39] He was buried in Algeria after lying in state in Tunisia. Later, his body was moved to a martyrs' (Chouhada) graveyard at Aïn Kerma in eastern Algeria.

Frantz Fanon was survived by his French wife, Josie (née Dublé), their son, Olivier Fanon, and his daughter from a previous relationship, Mireille Fanon-Mendès France. Josie Fanon later became disillusioned with the government and after years of depression and drinking died by suicide in Algiers in 1989.[31][40] Mireille became a professor of international law and conflict resolution and serves as president of the Frantz Fanon Foundation. Olivier became president of the Frantz Fanon National Association, which was created in Algiers in 2012.[41]

# Works

## Black Skin, White Masks

Black Skin, White Masks was first published in French as Peau noire, masques blancs in 1952 and is one of Fanon's most important works. In Black Skin, White Masks, Fanon psychoanalyzes the oppressed black person who is perceived to have to be a lesser creature in the white world that they live in, and studies how they navigate the world through a performance of Whiteness.[16] Particularly in discussing language, he talks about how the black person's use of a colonizer's language is seen by the colonizer as predatory, and not transformative, which in turn may create insecurity in the black's consciousness.[15] He recounts that he himself faced many admonitions as a child for using Creole French instead of "real French", or "French French", that is, "white" French.[16] Ultimately, he concludes that "mastery of language [of the white/colonizer] for the sake of recognition as white reflects a dependency that subordinates the black's humanity".[15]

The reception of his work has been affected by English translations which are recognized to contain numerous omissions and errors, while his unpublished work, including his doctoral thesis, has received little attention. As a result, it has been argued that Fanon has often been portrayed as an advocate of violence (it would be more accurate to characterize him as a dialectical opponent of nonviolence) and that his ideas have been extremely oversimplified. This reductionist vision of Fanon's work ignores the subtlety of his understanding of the colonial system. For example, the fifth chapter of Black Skin, White Masks translates, literally, as "The Lived Experience of the Black" ("L'expérience vécue du Noir"), but Markmann's translation is "The Fact of Blackness", which leaves out the massive influence of phenomenology on Fanon's early work.[42]

Black Skin, White Masks has been criticized as sexist and homophobic.[43] Among other statements, the book contains the remarks, "Just as there are faces that just ask to be slapped, couldn't we speak of women who just ask to be raped",[44] and "when a woman lives the fantasy of rape by a black man, it is a kind of fulfilment of a personal dream or an intimate wish",[45] and "the Negrophobic man is a repressed homosexual".[46]

## A Dying Colonialism

A Dying Colonialism is a 1959 book by Fanon that provides an account of how, during the Algerian Revolution, the people of Algeria fought their oppressors. They changed centuries-old cultural patterns and embraced certain ancient cultural practices long derided by their colonialist oppressors as "primitive," in order to destroy the oppressors. Fanon uses the fifth year of the Algerian Revolution as a point of departure for an explication of the inevitable dynamics of colonial oppression. The militant book describes Fanon's understanding that for the colonized, “having a gun is the only chance you still have of giving a meaning to your death.”[47] It also contains one of his most influential articles, "Unveiled Algeria", that signifies the fall of imperialism and describes how oppressed people struggle to decolonize their "mind" to avoid assimilation.

## The Wretched of the Earth

In The Wretched of the Earth (1961, Les damnés de la terre), published shortly before Fanon's death, Fanon defends the right of a colonized people to use violence to gain independence. In addition, he delineated the processes and forces leading to national independence or neocolonialism during the decolonization movement that engulfed much of the world after World War II. In defence of the use of violence by colonized peoples, Fanon argued that human beings who are not considered as such (by the colonizer) shall not be bound by principles that apply to humanity in their attitude towards the colonizer. His book was censored by the French government.

For Fanon in The Wretched of the Earth, the colonizer's presence in Algeria is based on sheer military strength. Any resistance to this strength must also be of a violent nature because it is the only "language" the colonizer speaks. Thus, violent resistance is a necessity imposed by the colonists upon the colonized. The relevance of language and the reformation of discourse pervades much of his work, which is why it is so interdisciplinary, spanning psychiatric concerns to encompass politics, sociology, anthropology, linguistics and literature.[48]

His participation in the Algerian Front de Libération Nationale from 1955 determined his audience as the Algerian colonized. It was to them that his final work, Les damnés de la terre (translated into English by Constance Farrington as The Wretched of the Earth) was directed. It constitutes a warning to the oppressed of the dangers they face in the whirlwind of decolonization and the transition to a neo-colonialist, globalized world.[49]
""";

private static final String SAMPLE_MARKDOWN2 = """
# World War II

After the Battle of France resulted in the French Third Republic capitulating to Nazi Germany in July 1940, Martinique came under the control of French Navy elements led by Admiral Georges Robert who were loyal to the collaborationist Vichy regime. The disruption of imports from Metropolitan France led to major shortages on the island, which were exacerbated by an American naval blockade imposed on Martinique in April 1943. Robert's authoritarian regime repressed local Allied sympathizers, hundreds of whom escaped to nearby Caribbean islands. Fanon later described the Vichy regime in Martinique as taking off their masks and behaving like "authentic racists".[20] In January 1943, he fled Martinique during the wedding of one of his brothers and travelled to the British colony of Dominica in order to link up with other Allied sympathizers.[21]: 24 

Robert's regime was overthrown by a local uprising in June of that year, which Fanon would later acclaim as "the birth of the [Martinican] proletariat" as a revolutionary force. After the uprising, Fanon "enthusiastically" returned to Martinique, where Free French leader Charles de Gaulle had appointed Henri Tourtet as the colony's new governor. Tourtet subsequently raised the 5th Antillean Marching Battalion to serve in Free French Forces (FFL), and Fanon soon joined the unit in Fort-de-France.[22][23] He underwent basic training before boarding a troopship bound for Casablanca, Morocco in March 1944. After Fanon arrived in Morocco, he was shocked to discover the extent of racial discrimination in the FFL. He was subsequently transferred to a Free French military base in Béjaïa, Algeria, where Fanon witnessed firsthand the antisemitism and Islamophobia of the pieds-noirs, many of whom had supported racist laws promulgated by the Vichy regime.[24]

In August 1944, he departed on another troopship from Oran to France as part of Operation Dragoon, the Allied invasion of German-occupied Provence. After the US VI Corps secured a beachhead, Fanon's unit came ashore at Saint-Tropez and advanced inland. He participated in several engagements near Montbéliard, Doubs and was seriously wounded by shrapnel, which resulted in him being hospitalized for two months. Fanon was awarded a Croix de Guerre by Colonel Raoul Salan for his actions in battle, and in early 1945 rejoined his unit and fought in the Battle of Alsace.[25] After German forces had been pushed out of France and Allied troops crossed the Rhine into Germany, Fanon and his fellow black troops were removed from their formations and sent southwards to Toulon as part of de Gaulle's policy of removing non-white soldiers from the French army.[12] He was subsequently transferred to Normandy to await repatriation.[26]

Although Fanon had been initially eager to participate in the Allied war effort, the racism he witnessed during the war disillusioned him. Fanon wrote to his brother Joby from Europe that "I've been deceived, and I am paying for my mistakes... I'm sick of it all."[16] In the fall of 1945, a newly-discharged Fanon returned to Martinique, where he focused on completing his secondary education. Césaire, by now a friend and mentor of his, ran on the French Communist Party ticket as a delegate from Martinique to the first National Assembly of the French Fourth Republic, and Fanon worked for his campaign. Staying in Martinique long enough to complete his baccalauréat, Fanon proceeded to return to France, where he intended to study medicine and psychiatry.[citation needed]

# France

Fanon was educated at the University of Lyon, where he also studied literature, drama and philosophy, sometimes attending Merleau-Ponty's lectures. During this period, he wrote three plays, of which two survive.[27] After qualifying as a psychiatrist in 1951, Fanon did a residency in psychiatry at Saint-Alban-sur-Limagnole under the radical Catalan psychiatrist François Tosquelles, who invigorated Fanon's thinking by emphasizing the role of culture in psychopathology.

In 1948, Fanon started a relationship with Michèle Weyer, a medical student, who soon became pregnant. He left her for an 18-year-old high school student, Josie, whom he married in 1952. At the urging of his friends, he later recognized his daughter, Mireille, although he did not have contact with her.[28] Paulin Joachim, who knew Fanon, said that on a number of occasions he had seen Fanon hit Josie.[29]

In France, while completing his residency, Fanon wrote and published his first book, Black Skin, White Masks (1952), an analysis of the negative psychological effects of colonial subjugation upon black people. Originally, the manuscript was the doctoral dissertation, submitted at Lyon, entitled Essay on the Disalienation of the Black, which was a response to the racism that Fanon experienced while studying psychiatry and medicine at the University in Lyon; the rejection of the dissertation prompted Fanon to publish it as a book. In 1951, for his doctor of medicine degree, he submitted another dissertation of narrower scope and a different subject (Altérations mentales, modifications caractérielles, troubles psychiques et déficit intellectuel dans l'hérédo-dégénération spino-cérébelleuse : à propos d'un cas de maladie de Friedreich avec délire de possession – Mental alterations, character modifications, psychic disorders, and intellectual deficit in hereditary spinocerebellar degeneration: A case of Friedreich's disease with delusions of possession). Left-wing philosopher Francis Jeanson, leader of the pro-Algerian independence Jeanson network, read Fanon's manuscript and, as a senior book editor at Éditions du Seuil in Paris, gave the book its new title and wrote its epilogue.[30]

After receiving Fanon's manuscript at Seuil, Jeanson invited him to an editorial meeting. Amid Jeanson's praise of the book, Fanon exclaimed: "Not bad for a nigger, is it?" Insulted, Jeanson dismissed Fanon from his office. Later, Jeanson learned that his response had earned him the writer's lifelong respect, and Fanon acceded to Jeanson's suggestion that the book be entitled Black Skin, White Masks.[30]

In the book, Fanon described the unfair treatment of black people in France and how they were disapproved of by white people. Frantz argued that racism and dehumanization directed toward black people caused feelings of inferiority among black people. This dehumanization prevented black people from fully assimilating into white society and, further, into full personhood. This caused psychological strife among black people, as even if they spoke French, obtained an education, and followed social customs associated with white people, they would still never be regarded as French, or a Man; instead, black people are defined as "Black Man" rather than "Man". (See further discussion of Black Skin, White Masks under Work, below.)

""";

    private static final String SAMPLE_MARKDOWN3 = """
After the Battle of France resulted in the French Third Republic ...
        """;

    private static final List<LinkPanel.AnchorItem> SAMPLE_LINKS = List.of(
        new LinkPanel.AnchorItem("Frantz Fanon", "https://en.wikipedia.org/wiki/Frantz_Fanon"),
        new LinkPanel.AnchorItem("Black Skin, White Masks", "https://en.wikipedia.org/wiki/Black_Skin,_White_Masks"),
        new LinkPanel.AnchorItem("The Wretched of the Earth", "https://en.wikipedia.org/wiki/The_Wretched_of_the_Earth"),
        new LinkPanel.AnchorItem("Battle of France", "https://en.wikipedia.org/wiki/Battle_of_France"),
        new LinkPanel.AnchorItem("Vichy France", "https://en.wikipedia.org/wiki/Vichy_France"),
        new LinkPanel.AnchorItem("Martinique", "https://en.wikipedia.org/wiki/Martinique"),
        new LinkPanel.AnchorItem("Free French Forces", "https://en.wikipedia.org/wiki/Free_French_Forces"),
        new LinkPanel.AnchorItem("Charles de Gaulle", "https://en.wikipedia.org/wiki/Charles_de_Gaulle"),
        new LinkPanel.AnchorItem("Operation Dragoon", "https://en.wikipedia.org/wiki/Operation_Dragoon"),
        new LinkPanel.AnchorItem("Battle of Alsace", "https://en.wikipedia.org/wiki/Battle_of_Alsace"),
        new LinkPanel.AnchorItem("Croix de Guerre", "https://en.wikipedia.org/wiki/Croix_de_Guerre"),
        new LinkPanel.AnchorItem("University of Lyon", "https://en.wikipedia.org/wiki/University_of_Lyon"),
        new LinkPanel.AnchorItem("Maurice Merleau-Ponty", "https://en.wikipedia.org/wiki/Maurice_Merleau-Ponty"),
        new LinkPanel.AnchorItem("Aim\u00e9 C\u00e9saire", "https://en.wikipedia.org/wiki/Aim%C3%A9_C%C3%A9saire"),
        new LinkPanel.AnchorItem("Jean-Paul Sartre", "https://en.wikipedia.org/wiki/Jean-Paul_Sartre"),
        new LinkPanel.AnchorItem("French Communist Party", "https://en.wikipedia.org/wiki/French_Communist_Party"),
        new LinkPanel.AnchorItem("Algerian Revolution", "https://en.wikipedia.org/wiki/Algerian_War"),
        new LinkPanel.AnchorItem("Front de Lib\u00e9ration Nationale", "https://en.wikipedia.org/wiki/National_Liberation_Front_(Algeria)"),
        new LinkPanel.AnchorItem("Blida-Joinville Hospital", "https://en.wikipedia.org/wiki/Blida"),
        new LinkPanel.AnchorItem("A Dying Colonialism", "https://en.wikipedia.org/wiki/A_Dying_Colonialism"),
        new LinkPanel.AnchorItem("Francis Jeanson", "https://en.wikipedia.org/wiki/Francis_Jeanson"),
        new LinkPanel.AnchorItem("Decolonization", "https://en.wikipedia.org/wiki/Decolonization"),
        new LinkPanel.AnchorItem("Phenomenology", "https://en.wikipedia.org/wiki/Phenomenology_(philosophy)"),
        new LinkPanel.AnchorItem("Postcolonialism", "https://en.wikipedia.org/wiki/Postcolonialism"),
        new LinkPanel.AnchorItem("N\u00e9gritude", "https://en.wikipedia.org/wiki/N%C3%A9gritude"),
        new LinkPanel.AnchorItem("Pieds-Noirs", "https://en.wikipedia.org/wiki/Pied-Noir"),
        new LinkPanel.AnchorItem("Saint-Tropez", "https://en.wikipedia.org/wiki/Saint-Tropez"),
        new LinkPanel.AnchorItem("Casablanca", "https://en.wikipedia.org/wiki/Casablanca"),
        new LinkPanel.AnchorItem("Raoul Salan", "https://en.wikipedia.org/wiki/Raoul_Salan"),
        new LinkPanel.AnchorItem("National Institutes of Health", "https://en.wikipedia.org/wiki/National_Institutes_of_Health")
    );

    private static final List<VariablePanel.VariableItem> SAMPLE_VARIABLES = List.of(
        new VariablePanel.VariableItem("Recipient Name", "recipient.name"),
        new VariablePanel.VariableItem("Recipient Email", "recipient.email"),
        new VariablePanel.VariableItem("Sender Name", "sender.name"),
        new VariablePanel.VariableItem("Sender Title", "sender.title"),
        new VariablePanel.VariableItem("Company Name", "company.name"),
        new VariablePanel.VariableItem("Review Period", "review.period"),
        new VariablePanel.VariableItem("Review Due Date", "review.dueDate"),
        new VariablePanel.VariableItem("Manager Name", "manager.name"),
        new VariablePanel.VariableItem("Department", "department"),
        new VariablePanel.VariableItem("Current Date", "date.current"),
        new VariablePanel.VariableItem("Fiscal Year", "fiscal.year"),
        new VariablePanel.VariableItem("Team Name", "team.name")
    );

    private static List<VariablePanel.VariableItem> filterVariables(String query) {
        if ((query == null) || query.isEmpty())
            return SAMPLE_VARIABLES;
        String lower = query.toLowerCase();
        List<VariablePanel.VariableItem> results = new ArrayList<>();
        for (VariablePanel.VariableItem item : SAMPLE_VARIABLES) {
            if (item.label().toLowerCase().contains(lower) || item.name().toLowerCase().contains(lower)) {
                results.add(item);
                if (results.size() >= 5)
                    break;
            }
        }
        return results;
    }

    private static List<LinkPanel.AnchorItem> filterLinks(String query) {
        if ((query == null) || query.isEmpty())
            return SAMPLE_LINKS.subList(0, 5);
        String lower = query.toLowerCase();
        List<LinkPanel.AnchorItem> results = new ArrayList<>();
        for (LinkPanel.AnchorItem item : SAMPLE_LINKS) {
            if (item.label().toLowerCase().contains(lower) || item.url().toLowerCase().contains(lower)) {
                results.add(item);
                if (results.size() >= 5)
                    break;
            }
        }
        return results;
    }

    public EditorExamples() {
        super (new Panel.Config ());

        FormattedTextEditor editor = add(new FormattedTextEditor(new FormattedTextEditor.Config()
            .editor(new Editor.Config()
                .debugLog(false))
            .height(Length.px(200))
            .position(Position.FLOATING)
            .toolbar(new EditorToolbar.Config()
                .tools(Tools.BOLD, Tools.ITALIC, Tools.UNDERLINE, Tools.STRIKETHROUGH,
                       Tools.SUBSCRIPT, Tools.SUPERSCRIPT, Tools.CODE, Tools.HIGHLIGHT,
                       Tools.SEPARATOR,
                       Tools.H1, Tools.H2, Tools.H3, Tools.PARAGRAPH,
                       Tools.SEPARATOR,
                       Tools.BULLET_LIST, Tools.NUMBERED_LIST,
                       Tools.SEPARATOR,
                       Tools.TABLE, Tools.SEPARATOR,
                       Tools.link(r -> Em.$(r).style(FontAwesome.link()), "Link", EditorExamples::filterLinks),
                       Tools.SEPARATOR,
                       Tools.variable("{}", "Variable", EditorExamples::filterVariables)))));
        editor.setValue(Value.of(MarkdownParser.parse(SAMPLE_MARKDOWN3)));
    }

}
