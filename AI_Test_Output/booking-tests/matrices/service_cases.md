**Service Booking — Test Matrix**

| ID | Function | Scenario | Inputs | Expected | Mocks |
|---|---|---|---|---|---|
| S1 | bookOneBike | Null dates | start=null, end=valid | IllegalArgumentException | IOrderService mocked to throw OR Fake impl validates |
| S2 | bookOneBike | start > end | start=2025-01-10, end=2025-01-05 | IllegalArgumentException | same as S1 |
| S3 | bookOneBike | 1-day booking | start=end=2025-01-10 | Success orderId>0 | Return orderId=123 from mock |
| S4 | bookOneBike | Bike not bookable | pricePerDay=null in DAO | IllegalStateException | Mock to throw with message |
| S5 | bookOneBike | Overlap existing confirmed | Overlap range intersects | IllegalStateException with details | Mock to throw |
| S6 | isBikeAvailable | No overlap | Any bike/date range | true | Mock returns true |
| S7 | isBikeAvailable | Overlap exists | Any bike/date range | false | Mock returns false |
| S8 | getOverlappingRanges | No records | Any inputs | Empty list | Mock returns [] |
| S9 | getOverlappingRanges | Multiple overlaps | Any inputs | Sorted ranges | Mock returns list ordered by start |
| S10 | search | Invalid date range | start>end | Empty list or handled error | Mock returns [] |
| S11 | search | Paging & sort | page/size/sort provided | Delegation to DAO OK | Verify interactions on mock |

