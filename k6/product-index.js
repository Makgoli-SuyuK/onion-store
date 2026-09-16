import http from 'k6/http';
import { check } from 'k6';
import { Trend } from 'k6/metrics';

const BASE_URL = 'http://spring:8080';

// 테스트 케이스별 성능 측정
const priceSearchDuration = new Trend('price_search_duration');
const likeCountSearchDuration = new Trend('like_count_search_duration');
const categoryPriceSearchDuration = new Trend('category_price_search_duration');
const nameSearchDuration = new Trend('name_search_duration');

export const options = {
    scenarios: {
        product_index_test: {
            executor: 'constant-vus',
            vus: 5,
            duration: '30s',
        },
    },

    thresholds: {
        http_req_failed: ['rate<0.01'],
    },
};

function search(params, metric) {
    const query = Object.entries(params)
        .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
        .join('&');

    const response = http.get(
        `${BASE_URL}/api/products?${query}`,
        {
            tags: {
                test_type: metric.name
            }
        }
    );

    check(response, {
        'status is 200': (r) => r.status === 200,
    });

    metric.add(response.timings.duration);

    return response;
}

export default function () {

    // 1. 가격 범위 검색
    search(
        {
            priceStart: 100000,
            priceEnd: 110000,
            page: 0,
            size: 20,
            sort: 'createdAt',
            direction: 'asc',
        },
        priceSearchDuration,
    );

    // 2. 좋아요 수 검색
    search(
        {
            likeCount: 9800,
            page: 0,
            size: 20,
            sort: 'createdAt',
            direction: 'asc',
        },
        likeCountSearchDuration,
    );

    // 3. 카테고리 + 가격 검색
    search(
        {
            category: 'category',
            priceStart: 100000,
            priceEnd: 110000,
            page: 0,
            size: 20,
            sort: 'createdAt',
            direction: 'asc',
        },
        categoryPriceSearchDuration,
    );

    // 5. 이름 포함 검색
    search(
        {
            name: 'product 500',
            page: 0,
            size: 20,
            sort: 'createdAt',
            direction: 'asc',
        },
        nameSearchDuration,
    );
}