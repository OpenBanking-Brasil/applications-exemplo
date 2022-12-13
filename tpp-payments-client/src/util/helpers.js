export function getPathWithQueryParams(queryParams) {
    let path = "";
    let isFirstIteration = true;
    for (let queryParam in queryParams) {
      if (queryParams[queryParam]) {
        if (!isFirstIteration) {
          path += `&${queryParam}=${queryParams[queryParam]}`;
        } else {
          isFirstIteration = false;
          path = `?${queryParam}=${queryParams[queryParam]}`;
        }
      }
    }
  
    return path;
  }