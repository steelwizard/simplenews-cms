package simplenews.cms.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import simplenews.cms.data.entity.Category;
import simplenews.cms.data.entity.CategoryTypeEnum;
import simplenews.cms.data.entity.News;
import simplenews.cms.data.entity.NewsFieldValue;
import simplenews.cms.data.entity.NewsFieldValueId;
import simplenews.cms.data.entity.ReviewField;
import simplenews.cms.data.entity.ReviewFieldTypeEnum;
import simplenews.cms.data.entity.ReviewType;
import simplenews.cms.data.entity.SiteSetting;
import simplenews.cms.data.entity.StaticPage;
import simplenews.cms.data.repository.CategoryRepository;
import simplenews.cms.data.repository.NewsFieldValueRepository;
import simplenews.cms.data.repository.NewsRepository;
import simplenews.cms.data.repository.ReviewFieldRepository;
import simplenews.cms.data.repository.ReviewTypeRepository;
import simplenews.cms.data.repository.SiteSettingRepository;
import simplenews.cms.data.repository.StaticPageRepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.logging.Logger;

@Component
@Order(2)
public class ExampleContentSeeder implements ApplicationRunner {

	private static final Logger LOG = Logger.getLogger(ExampleContentSeeder.class.getName());
	private static final String SEEDED_SETTING = "examples.seeded";
	private static final String ADMIN = "admin";

	@Autowired
	SiteSettingRepository siteSettingRepository;
	@Autowired
	ReviewTypeRepository reviewTypeRepository;
	@Autowired
	ReviewFieldRepository reviewFieldRepository;
	@Autowired
	CategoryRepository categoryRepository;
	@Autowired
	NewsRepository newsRepository;
	@Autowired
	NewsFieldValueRepository newsFieldValueRepository;
	@Autowired
	StaticPageRepository pageRepository;
	@Autowired
	INavigationService navigationService;

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (siteSettingRepository.existsById(SEEDED_SETTING)) return;

		Timestamp now = new Timestamp(System.currentTimeMillis());
		ReviewType album = reviewType("album", "Music album", now);
		ReviewField artist = field(album, "artist", "Artist", ReviewFieldTypeEnum.TEXT, null, null, null, true, 0, true, true);
		ReviewField year = field(album, "year", "Year", ReviewFieldTypeEnum.NUMBER, new BigDecimal("1900"), new BigDecimal("2100"), null, false, 1, true, false);
		ReviewField genre = field(album, "genre", "Genre", ReviewFieldTypeEnum.CHOICE, null, null,
				"Jazz\nRock\nPop\nElectronic\nHip-Hop\nOther", false, 2, true, true);
		ReviewField rating = field(album, "rating", "Rating", ReviewFieldTypeEnum.RATING, BigDecimal.ZERO, BigDecimal.TEN, null, true, 3, true, false);
		ReviewField replay = field(album, "replay", "Replay value", ReviewFieldTypeEnum.PERCENT, BigDecimal.ZERO, new BigDecimal("100"), null, false, 4, true, false);

		ReviewType game = reviewType("game", "Game", now);
		ReviewField publisher = field(game, "publisher", "Publisher", ReviewFieldTypeEnum.TEXT, null, null, null, true, 0, true, true);
		ReviewField platform = field(game, "platform", "Platform", ReviewFieldTypeEnum.CHOICE, null, null,
				"PC\nPlayStation\nXbox\nNintendo\nMobile\nOther", false, 1, true, true);
		ReviewField gameRating = field(game, "rating", "Rating", ReviewFieldTypeEnum.RATING, BigDecimal.ZERO, BigDecimal.TEN, null, true, 2, true, false);
		ReviewField score = field(game, "score", "Score", ReviewFieldTypeEnum.PERCENT, BigDecimal.ZERO, new BigDecimal("100"), null, false, 3, true, false);

		Category music = category("music", "Music", CategoryTypeEnum.REVIEW, album.getId(), now);
		Category games = category("games", "Games", CategoryTypeEnum.REVIEW, game.getId(), now);
		Category downloads = category("downloads", "Downloads", CategoryTypeEnum.DOWNLOAD, null, now);

		News kindOfBlue = article("kind-of-blue", "Kind of Blue",
				"<p>An example music review. The artist, year, genre and scores sit in the review fields.</p>",
				music, now);
		value(kindOfBlue, artist, "Miles Davis", null);
		value(kindOfBlue, genre, "Jazz", null);
		value(kindOfBlue, rating, "9.5", new BigDecimal("9.5"));
		value(kindOfBlue, replay, "92", new BigDecimal("92"));
		value(kindOfBlue, year, "1959", new BigDecimal("1959"));

		News exampleGame = article("example-game", "Example Game",
				"<p>An example game review. Change publisher, platform and scores to match a real title.</p>",
				games, now);
		value(exampleGame, publisher, "Example Studio", null);
		value(exampleGame, platform, "PC", null);
		value(exampleGame, gameRating, "8", new BigDecimal("8"));
		value(exampleGame, score, "81", new BigDecimal("81"));

		article("starter-pack", "Starter pack",
				"<p>An example download post. Edit it and attach a file (zip, pdf, image or video) from the Files box.</p>",
				downloads, now);

		if (!pageRepository.existsByKey("about")) {
			StaticPage about = new StaticPage();
			about.setKey("about");
			about.setTitle("About us");
			about.setContent("<p>This is an example About us page. Edit it under Pages in the admin area. "
					+ "It is published at <code>/pages/about.html</code>.</p>");
			about.setVisible(true);
			about.setCreator(ADMIN);
			about.setLastModifiedBy(ADMIN);
			about.setCreated(now);
			about.setLastModified(now);
			about.setMetaDescription("Who we are");
			pageRepository.save(about);
			LOG.info("Created example page about.");
		}

		SiteSetting flag = new SiteSetting();
		flag.setKey(SEEDED_SETTING);
		flag.setValue("true");
		siteSettingRepository.save(flag);
		navigationService.sync();
		LOG.info("Example categories, reviews, downloads and the About us page are in place.");
	}

	private ReviewType reviewType(String key, String title, Timestamp now) {
		return reviewTypeRepository.findByKey(key).orElseGet(() -> {
			ReviewType type = new ReviewType();
			type.setKey(key);
			type.setTitle(title);
			type.setCreator(ADMIN);
			type.setCreated(now);
			type.setLastModified(now);
			ReviewType saved = reviewTypeRepository.save(type);
			LOG.info("Created example review type " + key);
			return saved;
		});
	}

	private ReviewField field(ReviewType type, String key, String label, ReviewFieldTypeEnum fieldType,
							  BigDecimal min, BigDecimal max, String choices, boolean required, int position,
							  boolean showInList, boolean filterable) {
		return reviewFieldRepository.findByReviewTypeAndKey(type.getId(), key).orElseGet(() -> {
			ReviewField field = new ReviewField();
			field.setReviewType(type.getId());
			field.setKey(key);
			field.setLabel(label);
			field.setFieldType(fieldType);
			field.setMinValue(min);
			field.setMaxValue(max);
			field.setChoices(choices);
			field.setRequired(required);
			field.setPosition(position);
			field.setShowInList(showInList);
			field.setFilterable(filterable);
			field.setActive(true);
			return reviewFieldRepository.save(field);
		});
	}

	private Category category(String name, String title, CategoryTypeEnum type, Long reviewType, Timestamp now) {
		Category existing = categoryRepository.findByName(name);
		if (existing != null) return existing;
		Category category = new Category();
		category.setName(name);
		category.setTitle(title);
		category.setType(type);
		category.setReviewType(reviewType);
		category.setVisible(true);
		category.setCreator(ADMIN);
		category.setCreated(now);
		category.setLastModified(now);
		Category saved = categoryRepository.save(category);
		LOG.info("Created example category " + name);
		return saved;
	}

	private News article(String key, String title, String content, Category category, Timestamp now) {
		return newsRepository.findByKey(key).orElseGet(() -> {
			News news = new News();
			news.setKey(key);
			news.setTitle(title);
			news.setContent(content);
			news.setCategory(category);
			news.setCreator(ADMIN);
			news.setLastModifiedBy(ADMIN);
			news.setCreated(now);
			news.setLastModified(now);
			news.setVisible(true);
			News saved = newsRepository.save(news);
			LOG.info("Created example post " + key);
			return saved;
		});
	}

	private void value(News news, ReviewField field, String text, BigDecimal number) {
		if (news == null || field == null) return;
		if (newsFieldValueRepository.existsById(new NewsFieldValueId(news.getId(), field.getId()))) return;
		NewsFieldValue value = new NewsFieldValue();
		value.setNewsId(news.getId());
		value.setFieldId(field.getId());
		value.setValueText(text);
		value.setValueNumber(number);
		newsFieldValueRepository.save(value);
	}
}
