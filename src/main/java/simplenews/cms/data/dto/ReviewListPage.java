package simplenews.cms.data.dto;

import simplenews.cms.data.entity.ReviewType;

import java.util.List;
import java.util.Map;

/**
 * One page of the public review list: the articles, the values shown beside them,
 * and the links for sorting, filtering and paging.
 */
public record ReviewListPage(
		ReviewType type,
		String title,
		List<INewsDto> reviews,
		Map<Long, List<ReviewFieldDisplayDto>> displayValues,
		boolean hasRating,
		boolean byRating,
		boolean filtered,
		String newestUrl,
		String ratingUrl,
		String allUrl,
		String prevUrl,
		String nextUrl,
		int page
) {}
